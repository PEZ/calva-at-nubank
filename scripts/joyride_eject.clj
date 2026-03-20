(ns joyride-eject
  (:require [babashka.fs :as fs]
            [babashka.process :as p]
            [cheshire.core :as json]
            [clojure.edn :as edn]
            [clojure.string :as str]))

;; Section 8: ANSI Color Helpers

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

;; Section 9: Agent-friendly output

(defn- write-output! [filename content]
  (fs/create-dirs ".tmp")
  (spit (str ".tmp/" filename) content))

;; Section 1: Global Path Resolution

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

;; Section 2: Config Loading & Validation

(defn- load-config []
  (let [config-file "joyride-eject.edn"]
    (when-not (fs/exists? config-file)
      (throw (ex-info (str "Config file not found: " config-file)
                      {:babashka/exit 1
                       :file config-file})))
    (let [config (edn/read-string (slurp config-file))]
      (when-not (vector? (:files config))
        (throw (ex-info ":files must be a vector of strings"
                        {:babashka/exit 1
                         :config config})))
      (when-not (vector? (:npm-deps config))
        (throw (ex-info ":npm-deps must be a vector of strings"
                        {:babashka/exit 1
                         :config config})))
      config)))

;; Section 3: File Expansion

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

;; Section 4: Diff Checking

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

;; Section 5: npm Dep Checking

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

;; Section 6: File Operations

(defn- copy-files! [source-root target-root files]
  (doseq [file files]
    (let [source-path (fs/path source-root file)
          target-path (fs/path target-root file)
          parent-dir (fs/parent target-path)]
      (when parent-dir
        (fs/create-dirs parent-dir))
      (fs/copy source-path target-path {:replace-existing true})))
  files)

;; Section 7: npm Installation

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

;; Section 10: Reporting

(defn- format-target-display [direction target]
  (if (= direction :globalize)
    (str/replace target (str (fs/home)) "~")
    target))

(defn- print-report! [direction plan result]
  (let [target-display (format-target-display direction (:target plan))
        {:keys [files npm-deps outcome]} result]
    (case outcome
      :aborted
      (do
        (println (red "✗ Aborted: source files not found"))
        (println)
        (when (seq (:errors files))
          (println "  Missing from" (:source plan) ":")
          (doseq [{:keys [entry type]} (:errors files)]
            (println "   " entry))
          (println)
          (println "  Check joyride-eject.edn :files entries")))

      :blocked
      (do
        (println (red (str "✗ Blocked: " (count (:conflicts files)) " file" (when (> (count (:conflicts files)) 1) "s") " differ")))
        (println)
        (when (seq (:conflicts files))
          (println "  Conflicts:")
          (doseq [{:keys [file diff]} (:conflicts files)]
            (println "   " file)
            (doseq [line (str/split-lines diff)]
              (println "     " line))))
        (when (seq (:to-copy files))
          (println)
          (println "  Would have copied:")
          (doseq [file (:to-copy files)]
            (println "   " file)))
        (when (seq (:identical files))
          (println)
          (println "  Already matching:")
          (doseq [file (:identical files)]
            (println "   " file)))
        (println)
        (println "  Resolve conflicts first, then re-run:"
                 (if (= direction :localize) "bb localize" "bb globalize")))

      :in-sync
      (do
        (println (yellow "ℹ Already in sync -- nothing to do"))
        (println)
        (when (seq (:identical files))
          (println " " (count (:identical files)) "files match"))
        (when (seq (:identical npm-deps))
          (println " " (count (:identical npm-deps)) "npm deps present")))

      :success
      (let [action (if (= direction :localize) "Localized" "Globalized")
            file-count (count (:copied files))
            dep-count (count (:installed npm-deps))]
        (println (green (str "✓ " action " " file-count " file" (when (> file-count 1) "s")
                             (when (seq (:installed npm-deps))
                               (str ", installed " dep-count " npm dep" (when (> dep-count 1) "s"))))))
        (println)
        (when (seq (:copied files))
          (println "  Copied:")
          (doseq [file (:copied files)]
            (println "   " file)))
        (when (seq (:installed npm-deps))
          (println)
          (println "  npm:" (str/join ", " (:installed npm-deps)) "->" target-display))))))

;; Section 11: Orchestration

(defn sync!
  "Entry point called from bb.edn task."
  [opts]
  (let [direction (:direction opts)]
    (when-not (#{:localize :globalize} direction)
      (throw (ex-info "Invalid direction, must be :localize or :globalize"
                      {:babashka/exit 1
                       :direction direction})))

    (try
      (let [global-dir (resolve-global-joyride-dir)
            [source-root target-root] (if (= direction :localize)
                                        [global-dir ".joyride/"]
                                        [".joyride/" global-dir])
            config (load-config)
            {:keys [files errors]} (expand-file-entries source-root (:files config))]

        (when (seq errors)
          (let [result {:direction direction
                        :source source-root
                        :target target-root
                        :files {:errors errors}
                        :npm-deps {}
                        :outcome :aborted}]
            (write-output! "joyride-eject-result.edn" (pr-str result))
            (print-report! direction {:source source-root :target target-root} result)
            (System/exit 1)))

        (let [file-diffs (check-file-diffs source-root target-root files)
              npm-check (check-npm-deps target-root (:npm-deps config))]

          (when (seq (:conflicts file-diffs))
            (let [result {:direction direction
                          :source source-root
                          :target target-root
                          :files file-diffs
                          :npm-deps npm-check
                          :outcome :blocked}]
              (write-output! "joyride-eject-result.edn" (pr-str result))
              (print-report! direction {:source source-root :target target-root} result)
              (System/exit 1)))

          (when (and (empty? (:to-copy file-diffs))
                     (empty? (:to-add npm-check)))
            (let [result {:direction direction
                          :source source-root
                          :target target-root
                          :files file-diffs
                          :npm-deps npm-check
                          :outcome :in-sync}]
              (write-output! "joyride-eject-result.edn" (pr-str result))
              (print-report! direction {:source source-root :target target-root} result)
              (System/exit 0)))

          ;; Create target directory if it doesn't exist (for localize)
          (when (and (= direction :localize) (not (fs/exists? target-root)))
            (fs/create-dirs target-root))

          (let [copied (when (seq (:to-copy file-diffs))
                         (copy-files! source-root target-root (:to-copy file-diffs)))
                installed (when (seq (:to-add npm-check))
                            (install-npm-deps! target-root (:to-add npm-check)))
                result {:direction direction
                        :source source-root
                        :target target-root
                        :files (assoc file-diffs :copied copied)
                        :npm-deps (assoc npm-check :installed installed)
                        :outcome :success}]
            (write-output! "joyride-eject-result.edn" (pr-str result))
            (print-report! direction {:source source-root :target target-root} result))))

      (catch clojure.lang.ExceptionInfo e
        (if (:babashka/exit (ex-data e))
          (do
            (println (red (.getMessage e)))
            (System/exit 1))
          (throw e))))))
