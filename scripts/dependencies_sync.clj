(ns dependencies-sync
  (:require [babashka.fs :as fs]
            [babashka.process :as p]
            [cheshire.core :as json]
            [clojure.edn :as edn]
            [clojure.string :as str]))

;; ANSI Color Helpers (no dependencies on later functions)

(defn- colorize [color text]
  (if (System/getenv "NO_COLOR")
    text
    (str color text "\033[0m")))

(defn- green [text]
  (colorize "\033[32m" text))

(defn- red [text]
  (colorize "\033[31m" text))

(defn- yellow [text]
  (colorize "\033[33m" text))

;; Agent-friendly output

(defn- write-output! [filename content]
  (fs/create-dirs ".tmp")
  (spit (str ".tmp/" filename) content))

;; Global Path Resolution

(defn- resolve-global-joyride-dir []
  (let [joyride-config (System/getenv "JOYRIDE_CONFIG_DIR")
        xdg-config (System/getenv "XDG_CONFIG_HOME")
        resolved (cond
                   joyride-config joyride-config
                   xdg-config (str xdg-config "/joyride")
                   :else (str (fs/home) "/.config/joyride"))]
    (when-not (fs/exists? resolved)
      (throw (ex-info (str "Global Joyride directory not found: " resolved)
                      {:babashka/exit 1
                       :path resolved})))
    (str (fs/absolutize resolved))))

(defn- resolve-global-copilot-skills-dir []
  (let [resolved (str (fs/home) "/.copilot/skills/")]
    (when-not (fs/exists? resolved)
      (throw (ex-info (str "Global Copilot skills directory not found: " resolved)
                      {:babashka/exit 1
                       :path resolved})))
    (str (fs/absolutize resolved))))

;; Config Loading & Validation

(defn- load-config []
  (let [config-file "dependencies-sync.edn"]
    (when-not (fs/exists? config-file)
      (throw (ex-info (str "Config file not found: " config-file)
                      {:babashka/exit 1
                       :file config-file})))
    (let [config (edn/read-string (slurp config-file))
          joyride-files (get config :sync/joyride-files [])
          npm-deps (get config :sync/npm-deps [])
          skills (get config :sync/skills [])]
      ;; Validate types
      (when-not (or (nil? joyride-files) (vector? joyride-files))
        (throw (ex-info ":sync/joyride-files must be a vector of strings"
                        {:babashka/exit 1
                         :config config})))
      (when-not (or (nil? npm-deps) (vector? npm-deps))
        (throw (ex-info ":sync/npm-deps must be a vector of strings"
                        {:babashka/exit 1
                         :config config})))
      (when-not (or (nil? skills) (vector? skills))
        (throw (ex-info ":sync/skills must be a vector of strings"
                        {:babashka/exit 1
                         :config config})))
      ;; Warn if all empty
      (when (and (empty? joyride-files) (empty? npm-deps) (empty? skills))
        (println (yellow "⚠ Warning: all sync entries are empty")))
      ;; Return normalized config with defaults
      {:sync/joyride-files (or joyride-files [])
       :sync/npm-deps (or npm-deps [])
       :sync/skills (or skills [])})))

;; File Expansion

(defn- glob-pattern? [s]
  (some #(str/includes? s (str %)) ["*" "?" "{" "["]))

(defn- expand-file-entries [source-root entries]
  (let [results (for [entry entries]
                  (if (glob-pattern? entry)
                    (let [matches (fs/glob source-root entry)
                          relative-matches (map #(str (fs/relativize source-root %)) matches)
                          filtered (remove #(str/includes? % "node_modules") relative-matches)]
                      (if (empty? filtered)
                        {:error {:entry entry :type :no-match}}
                        {:files filtered}))
                    (let [full-path (fs/path source-root entry)]
                      (if (fs/exists? full-path)
                        (if (str/includes? entry "node_modules")
                          {:files []}
                          {:files [entry]})
                        {:error {:entry entry :type :missing-file}}))))
        errors (keep :error results)
        files (mapcat :files results)]
    {:files (vec files)
     :errors (vec errors)}))

;; Skills Expansion

(defn- expand-skill-entries [source-root skill-names]
  (let [results (for [entry skill-names]
                  (if (glob-pattern? entry)
                    (let [matches (fs/glob source-root entry)
                          relative-matches (map #(str (fs/relativize source-root %)) matches)
                          filtered (remove #(str/includes? % "node_modules") relative-matches)]
                      (if (empty? filtered)
                        {:error {:entry entry :type :no-match}}
                        {:files filtered}))
                    (let [skill-dir (fs/path source-root entry)]
                      (if-not (fs/exists? skill-dir)
                        {:error {:entry entry :type :missing-skill}}
                        (let [matches (fs/glob source-root (str entry "/**"))
                              relative-matches (map #(str (fs/relativize source-root %)) matches)
                              filtered (remove #(str/includes? % "node_modules") relative-matches)]
                          {:files filtered})))))
        errors (keep :error results)
        files (mapcat :files results)]
    {:files (vec files)
     :errors (vec errors)}))

;; Diff Checking

(defn- check-file-diffs [source-root target-root files]
  (let [results (for [file files]
                  (let [source-path (str (fs/path source-root file))
                        target-path (str (fs/path target-root file))]
                    (if-not (fs/exists? target-path)
                      {:file file :status :to-copy}
                      (let [result (p/shell {:out :string
                                             :err :string
                                             :continue true}
                                            "diff" "-bB" source-path target-path)]
                        (case (:exit result)
                          0 {:file file :status :identical}
                          1 {:file file :status :conflicts :diff (:out result)}
                          {:file file :status :conflicts :diff "Binary files differ"})))))]
    {:to-copy (vec (map :file (filter #(= :to-copy (:status %)) results)))
     :identical (vec (map :file (filter #(= :identical (:status %)) results)))
     :conflicts (vec (keep #(when (= :conflicts (:status %))
                              {:file (:file %)
                               :diff (:diff %)})
                           results))}))

;; npm Dep Checking

(defn- check-npm-deps [target-dir dep-names]
  (let [package-json-path (fs/path target-dir "package.json")
        existing-deps (when (fs/exists? package-json-path)
                        (-> (str package-json-path)
                            slurp
                            (json/parse-string true)
                            :dependencies
                            keys
                            set))
        results (for [dep dep-names]
                  (if (and existing-deps (contains? existing-deps (keyword dep)))
                    {:dep dep :status :identical}
                    {:dep dep :status :to-add}))]
    {:to-add (vec (map :dep (filter #(= :to-add (:status %)) results)))
     :identical (vec (map :dep (filter #(= :identical (:status %)) results)))}))

;; File Operations

(defn- copy-files! [source-root target-root files]
  (doseq [file files]
    (let [source-path (fs/path source-root file)
          target-path (fs/path target-root file)
          parent-dir (fs/parent target-path)]
      (when parent-dir
        (fs/create-dirs parent-dir))
      (fs/copy source-path target-path {:replace-existing true})))
  files)

;; npm Installation

(defn- install-npm-deps! [target-dir dep-names]
  (when (seq dep-names)
    (let [package-json-path (fs/path target-dir "package.json")]
      (when-not (fs/exists? package-json-path)
        (p/shell {:dir target-dir} "npm" "init" "-y"))
      (let [result (apply p/shell {:dir target-dir
                                   :out :string
                                   :err :string
                                   :continue true}
                          "npm" "install" dep-names)]
        (when-not (zero? (:exit result))
          (println (:out result))
          (println (:err result))
          (throw (ex-info "npm install failed"
                          {:babashka/exit 1
                           :deps dep-names}))))))
  dep-names)

;; Reporting

(defn- format-dir-display [dir]
  (str/replace (str dir) (str (fs/home)) "~"))

(defn- print-report! [direction result]
  (let [{:keys [joyride skills outcome]} result]
    (case outcome
      :aborted
      (do
        (println (red "✗ Aborted: source entries not found"))
        (println)
        (when (seq (get-in joyride [:files :errors]))
          (println (str "  Joyride -- missing from " (format-dir-display (:source joyride)) ":"))
          (doseq [{:keys [entry]} (get-in joyride [:files :errors])]
            (println "   " entry))
          (println))
        (when (seq (get-in skills [:files :errors]))
          (println (str "  Skills -- missing from " (format-dir-display (:source skills)) ":"))
          (doseq [{:keys [entry]} (get-in skills [:files :errors])]
            (println "   " entry))
          (println))
        (println "  Check dependencies-sync.edn entries"))

      :blocked
      (let [joyride-conflicts (get-in joyride [:files :conflicts])
            skills-conflicts (get-in skills [:files :conflicts])
            total-conflicts (+ (count joyride-conflicts) (count skills-conflicts))]
        (println (red (str "✗ Blocked: " total-conflicts " file" (when (> total-conflicts 1) "s") " differ")))
        (println)
        (when (seq joyride-conflicts)
          (println "  Joyride conflicts:")
          (doseq [{:keys [file diff]} joyride-conflicts]
            (println "   " file)
            (doseq [line (str/split-lines diff)]
              (println "     " line)))
          (println))
        (when (seq skills-conflicts)
          (println "  Skills conflicts:")
          (doseq [{:keys [file diff]} skills-conflicts]
            (println "   " file)
            (doseq [line (str/split-lines diff)]
              (println "     " line)))
          (println))
        (let [joyride-to-copy (get-in joyride [:files :to-copy])
              skills-to-copy (get-in skills [:files :to-copy])]
          (when (or (seq joyride-to-copy) (seq skills-to-copy))
            (println "  Would have copied:")
            (when (seq joyride-to-copy)
              (doseq [file joyride-to-copy]
                (println "    Joyride:" file)))
            (when (seq skills-to-copy)
              (doseq [file skills-to-copy]
                (println "    Skills:" file)))
            (println)))
        (println "  Resolve conflicts first, then re-run:"
                 (if (= direction :localize) "bb localize" "bb globalize")))

      :in-sync
      (do
        (println (yellow "ℹ Already in sync -- nothing to do"))
        (println)
        (let [joyride-files-count (count (get-in joyride [:files :identical]))
              joyride-npm-count (count (get-in joyride [:npm-deps :identical]))
              skills-files-count (count (get-in skills [:files :identical]))]
          (when (or (pos? joyride-files-count) (pos? joyride-npm-count))
            (println (str "  Joyride: "
                          joyride-files-count " files match"
                          (when (pos? joyride-npm-count)
                            (str ", " joyride-npm-count " npm deps present")))))
          (when (pos? skills-files-count)
            (println "  Skills:" (str skills-files-count " files match")))))

      :success
      (let [joyride-copied (get-in joyride [:files :copied])
            skills-copied (get-in skills [:files :copied])
            npm-installed (get-in joyride [:npm-deps :installed])
            action (if (= direction :localize) "Localized" "Globalized")
            parts (cond-> []
                    (seq joyride-copied) (conj (str (count joyride-copied) " joyride file" (when (> (count joyride-copied) 1) "s")))
                    (seq skills-copied) (conj (str (count skills-copied) " skill file" (when (> (count skills-copied) 1) "s")))
                    (seq npm-installed) (conj (str "installed " (count npm-installed) " npm dep" (when (> (count npm-installed) 1) "s"))))]
        (println (green (str "✓ " action " " (str/join ", " parts))))
        (println)
        (when (seq joyride-copied)
          (println "  Joyride copied:")
          (doseq [file joyride-copied]
            (println "   " file))
          (println))
        (when (seq skills-copied)
          (println "  Skills copied:")
          (doseq [file skills-copied]
            (println "   " file))
          (println))
        (when (seq npm-installed)
          (println "  npm:" (str/join ", " npm-installed) "->" (:target joyride)))))))

;; Orchestration

(defn sync!
  "Entry point called from bb.edn task."
  [opts]
  (let [direction (:direction opts)]
    (when-not (#{:localize :globalize} direction)
      (throw (ex-info "Invalid direction, must be :localize or :globalize"
                      {:babashka/exit 1
                       :direction direction})))

    (try
      ;; Resolve both global directories
      (let [global-joyride-dir (resolve-global-joyride-dir)
            global-skills-dir (resolve-global-copilot-skills-dir)

            ;; Set source/target for each concern
            [joyride-source joyride-target] (if (= direction :localize)
                                              [global-joyride-dir ".joyride/"]
                                              [".joyride/" global-joyride-dir])
            [skills-source skills-target] (if (= direction :localize)
                                            [global-skills-dir ".github/skills/"]
                                            [".github/skills/" global-skills-dir])

            ;; Load config
            config (load-config)

            ;; Expand both concerns
            joyride-expansion (expand-file-entries joyride-source (:sync/joyride-files config))
            skills-expansion (expand-skill-entries skills-source (:sync/skills config))]

        ;; Check for expansion errors in EITHER concern
        (when (or (seq (:errors joyride-expansion))
                  (seq (:errors skills-expansion)))
          (let [result {:direction direction
                        :joyride {:source joyride-source
                                  :target joyride-target
                                  :files joyride-expansion
                                  :npm-deps {}}
                        :skills {:source skills-source
                                 :target skills-target
                                 :files skills-expansion}
                        :outcome :aborted}]
            (write-output! "dependencies-sync-result.edn" (pr-str result))
            (print-report! direction result)
            (System/exit 1)))

        ;; Diff-check both concerns
        (let [joyride-file-diffs (check-file-diffs joyride-source joyride-target (:files joyride-expansion))
              skills-file-diffs (check-file-diffs skills-source skills-target (:files skills-expansion))
              npm-check (check-npm-deps joyride-target (:sync/npm-deps config))]

          ;; Check for conflicts in EITHER concern
          (when (or (seq (:conflicts joyride-file-diffs))
                    (seq (:conflicts skills-file-diffs)))
            (let [result {:direction direction
                          :joyride {:source joyride-source
                                    :target joyride-target
                                    :files joyride-file-diffs
                                    :npm-deps npm-check}
                          :skills {:source skills-source
                                   :target skills-target
                                   :files skills-file-diffs}
                          :outcome :blocked}]
              (write-output! "dependencies-sync-result.edn" (pr-str result))
              (print-report! direction result)
              (System/exit 1)))

          ;; Check if in-sync (nothing to do)
          (when (and (empty? (:to-copy joyride-file-diffs))
                     (empty? (:to-copy skills-file-diffs))
                     (empty? (:to-add npm-check)))
            (let [result {:direction direction
                          :joyride {:source joyride-source
                                    :target joyride-target
                                    :files joyride-file-diffs
                                    :npm-deps npm-check}
                          :skills {:source skills-source
                                   :target skills-target
                                   :files skills-file-diffs}
                          :outcome :in-sync}]
              (write-output! "dependencies-sync-result.edn" (pr-str result))
              (print-report! direction result)
              (System/exit 0)))

          ;; Create target directories if needed (for localize)
          (when (= direction :localize)
            (when (and (seq (:to-copy joyride-file-diffs))
                       (not (fs/exists? joyride-target)))
              (fs/create-dirs joyride-target))
            (when (and (seq (:to-copy skills-file-diffs))
                       (not (fs/exists? skills-target)))
              (fs/create-dirs skills-target)))

          ;; Execute: copy joyride files + install npm deps + copy skill files
          (let [joyride-copied (when (seq (:to-copy joyride-file-diffs))
                                 (copy-files! joyride-source joyride-target (:to-copy joyride-file-diffs)))
                npm-installed (when (seq (:to-add npm-check))
                                (install-npm-deps! joyride-target (:to-add npm-check)))
                skills-copied (when (seq (:to-copy skills-file-diffs))
                                (copy-files! skills-source skills-target (:to-copy skills-file-diffs)))
                result {:direction direction
                        :joyride {:source joyride-source
                                  :target joyride-target
                                  :files (assoc joyride-file-diffs :copied joyride-copied)
                                  :npm-deps (assoc npm-check :installed npm-installed)}
                        :skills {:source skills-source
                                 :target skills-target
                                 :files (assoc skills-file-diffs :copied skills-copied)}
                        :outcome :success}]
            (write-output! "dependencies-sync-result.edn" (pr-str result))
            (print-report! direction result))))

      (catch Exception e
        (if-let [exit-code (-> e ex-data :babashka/exit)]
          (do
            (println (red (.getMessage e)))
            (System/exit exit-code))
          (throw e))))))
