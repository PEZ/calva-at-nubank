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

(defn- resolve-global-copilot-dir []
  (let [resolved (str (fs/home) "/.copilot/")]
    (when-not (fs/exists? resolved)
      (throw (ex-info (str "Global Copilot directory not found: " resolved)
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
          skills (get config :sync/skills [])
          agents (get config :sync/agents [])
          prompts (get config :sync/prompts [])
          instructions (get config :sync/instructions [])]
      ;; Validate vector types
      (doseq [[k v] [[":sync/joyride-files" joyride-files]
                      [":sync/npm-deps" npm-deps]
                      [":sync/skills" skills]
                      [":sync/agents" agents]
                      [":sync/prompts" prompts]
                      [":sync/instructions" instructions]]]
        (when-not (or (nil? v) (vector? v))
          (throw (ex-info (str k " must be a vector of strings")
                          {:babashka/exit 1
                           :config config}))))
      ;; Warn if all empty
      (when (and (empty? joyride-files) (empty? npm-deps) (empty? skills)
                 (empty? agents) (empty? prompts) (empty? instructions))
        (println (yellow "⚠ Warning: all sync entries are empty")))
      {:sync/joyride-files (or joyride-files [])
       :sync/npm-deps (or npm-deps [])
       :sync/skills (or skills [])
       :sync/agents (or agents [])
       :sync/prompts (or prompts [])
       :sync/instructions (or instructions [])})))

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

;; Named File Expansion (agents, prompts)

(defn- expand-named-entries [source-root entries suffix]
  (let [results (for [entry entries]
                  (let [filename (str entry suffix)
                        full-path (fs/path source-root filename)]
                    (if (fs/exists? full-path)
                      {:files [filename]}
                      {:error {:entry entry :type :missing-file}})))
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

(defn- print-report! [direction {:keys [concerns npm-deps outcome]}]
  (case outcome
    :aborted
    (do
      (println (red "✗ Aborted: source entries not found"))
      (println)
      (doseq [{:concern/keys [name source expansion]} concerns]
        (when (seq (:errors expansion))
          (println (str "  " name " -- missing from " (format-dir-display source) ":"))
          (doseq [{:keys [entry]} (:errors expansion)]
            (println "   " entry))
          (println)))
      (println "  Check dependencies-sync.edn entries"))

    :blocked
    (let [all-conflicts (mapcat #(get-in % [:concern/diffs :conflicts]) concerns)
          total (count all-conflicts)]
      (println (red (str "✗ Blocked: " total " file" (when (> total 1) "s") " differ")))
      (println)
      (doseq [{:concern/keys [name diffs]} concerns]
        (when (seq (:conflicts diffs))
          (println (str "  " name " conflicts:"))
          (doseq [{:keys [file diff]} (:conflicts diffs)]
            (println "   " file)
            (doseq [line (str/split-lines diff)]
              (println "     " line)))
          (println)))
      (let [all-to-copy (for [{:concern/keys [name diffs]} concerns
                              file (:to-copy diffs)]
                          {:name name :file file})]
        (when (seq all-to-copy)
          (println "  Would have copied:")
          (doseq [{:keys [name file]} all-to-copy]
            (println (str "    " name ": " file)))
          (println)))
      (println "  Resolve conflicts first, then re-run:"
               (if (= direction :localize) "bb localize" "bb globalize")))

    :in-sync
    (do
      (println (yellow "ℹ Already in sync -- nothing to do"))
      (println)
      (doseq [{:concern/keys [name diffs]} concerns]
        (let [file-count (count (:identical diffs))]
          (when (pos? file-count)
            (println (str "  " name ": " file-count " file"
                          (when (> file-count 1) "s") " match")))))
      (when npm-deps
        (let [npm-count (count (:identical npm-deps))]
          (when (pos? npm-count)
            (println (str "  npm: " npm-count " dep"
                          (when (> npm-count 1) "s") " present"))))))

    :success
    (let [action (if (= direction :localize) "Localized" "Globalized")
          copied-parts (keep (fn [{:concern/keys [name diffs]}]
                               (let [copied (:copied diffs)]
                                 (when (seq copied)
                                   (str (count copied) " " (str/lower-case name) " file"
                                        (when (> (count copied) 1) "s")))))
                             concerns)
          npm-installed (:installed npm-deps)
          parts (cond-> (vec copied-parts)
                  (seq npm-installed)
                  (conj (str "installed " (count npm-installed) " npm dep"
                             (when (> (count npm-installed) 1) "s"))))]
      (println (green (str "✓ " action " " (str/join ", " parts))))
      (println)
      (doseq [{:concern/keys [name diffs]} concerns]
        (when (seq (:copied diffs))
          (println (str "  " name " copied:"))
          (doseq [file (:copied diffs)]
            (println "   " file))
          (println)))
      (when (seq npm-installed)
        (let [joyride-target (:concern/target
                              (first (filter #(= "Joyride" (:concern/name %)) concerns)))]
          (println "  npm:" (str/join ", " npm-installed) "->" joyride-target))))))

;; Concern Building

(defn- build-concerns [direction config global-joyride-dir global-copilot-dir]
  (let [copilot-skills-dir (str (fs/path global-copilot-dir "skills"))
        copilot-agents-dir (str (fs/path global-copilot-dir "agents"))
        copilot-prompts-dir (str (fs/path global-copilot-dir "prompts"))
        copilot-instructions-dir (str (fs/path global-copilot-dir "instructions"))
        dir-pair (fn [global local]
                   (if (= direction :localize) [global local] [local global]))]
    (cond-> []
      (or (seq (:sync/joyride-files config)) (seq (:sync/npm-deps config)))
      (conj (let [[s t] (dir-pair global-joyride-dir ".joyride/")]
              {:concern/name "Joyride"
               :concern/source s
               :concern/target t
               :concern/expand-fn expand-file-entries
               :concern/config-entries (:sync/joyride-files config)
               :concern/npm-deps (:sync/npm-deps config)}))

      (seq (:sync/skills config))
      (conj (let [[s t] (dir-pair copilot-skills-dir ".github/skills/")]
              {:concern/name "Skills"
               :concern/source s
               :concern/target t
               :concern/expand-fn expand-skill-entries
               :concern/config-entries (:sync/skills config)}))

      (seq (:sync/agents config))
      (conj (let [[s t] (dir-pair copilot-agents-dir ".github/agents/")]
              {:concern/name "Agents"
               :concern/source s
               :concern/target t
               :concern/expand-fn (fn [source entries]
                                    (expand-named-entries source entries ".agent.md"))
               :concern/config-entries (:sync/agents config)}))

      (seq (:sync/prompts config))
      (conj (let [[s t] (dir-pair copilot-prompts-dir ".github/prompts/")]
              {:concern/name "Prompts"
               :concern/source s
               :concern/target t
               :concern/expand-fn (fn [source entries]
                                    (expand-named-entries source entries ".prompt.md"))
               :concern/config-entries (:sync/prompts config)}))

      (seq (:sync/instructions config))
      (conj (let [[s t] (dir-pair copilot-instructions-dir ".github/instructions/")]
              {:concern/name "Instructions"
               :concern/source s
               :concern/target t
               :concern/expand-fn (fn [source entries]
                                    (expand-named-entries source entries ".instructions.md"))
               :concern/config-entries (:sync/instructions config)})))))

(defn- expand-concern [{:concern/keys [expand-fn source config-entries] :as concern}]
  (let [expansion (if (seq config-entries)
                    (expand-fn source config-entries)
                    {:files [] :errors []})]
    (assoc concern :concern/expansion expansion)))

(defn- diff-concern [{:concern/keys [source target expansion] :as concern}]
  (assoc concern :concern/diffs
         (check-file-diffs source target (:files expansion))))

(defn- copy-concern! [{:concern/keys [source target diffs] :as concern}]
  (if (seq (:to-copy diffs))
    (let [copied (copy-files! source target (:to-copy diffs))]
      (assoc-in concern [:concern/diffs :copied] copied))
    concern))

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
      (let [global-joyride-dir (resolve-global-joyride-dir)
            global-copilot-dir (resolve-global-copilot-dir)
            config (load-config)
            concerns (build-concerns direction config global-joyride-dir global-copilot-dir)

            ;; Phase 1: Expand all concerns
            concerns (mapv expand-concern concerns)]

        ;; Phase 2: Check expansion errors
        (when (some #(seq (get-in % [:concern/expansion :errors])) concerns)
          (let [result {:direction direction :concerns concerns :outcome :aborted}]
            (write-output! "dependencies-sync-result.edn" (pr-str result))
            (print-report! direction result)
            (System/exit 1)))

        ;; Phase 3: Diff all concerns
        (let [concerns (mapv diff-concern concerns)
              ;; npm deps (joyride-only sub-concern)
              joyride (first (filter #(= "Joyride" (:concern/name %)) concerns))
              npm-check (when (:concern/npm-deps joyride)
                          (check-npm-deps (:concern/target joyride)
                                          (:concern/npm-deps joyride)))]

          ;; Phase 4: Check conflicts
          (when (some #(seq (get-in % [:concern/diffs :conflicts])) concerns)
            (let [result {:direction direction :concerns concerns
                          :npm-deps npm-check :outcome :blocked}]
              (write-output! "dependencies-sync-result.edn" (pr-str result))
              (print-report! direction result)
              (System/exit 1)))

          ;; Phase 5: Check in-sync
          (when (and (not-any? #(seq (get-in % [:concern/diffs :to-copy])) concerns)
                     (or (nil? npm-check) (empty? (:to-add npm-check))))
            (let [result {:direction direction :concerns concerns
                          :npm-deps npm-check :outcome :in-sync}]
              (write-output! "dependencies-sync-result.edn" (pr-str result))
              (print-report! direction result)
              (System/exit 0)))

          ;; Phase 6: Create target directories and execute
          (when (= direction :localize)
            (doseq [{:concern/keys [target diffs]} concerns]
              (when (and (seq (:to-copy diffs)) (not (fs/exists? target)))
                (fs/create-dirs target))))

          (let [concerns (mapv copy-concern! concerns)
                npm-installed (when (seq (:to-add npm-check))
                                (install-npm-deps! (:concern/target joyride)
                                                   (:to-add npm-check)))
                npm-check (if npm-installed
                            (assoc npm-check :installed npm-installed)
                            npm-check)
                result {:direction direction :concerns concerns
                        :npm-deps npm-check :outcome :success}]
            (write-output! "dependencies-sync-result.edn" (pr-str result))
            (print-report! direction result))))

      (catch Exception e
        (if-let [exit-code (-> e ex-data :babashka/exit)]
          (do
            (println (red (.getMessage e)))
            (System/exit exit-code))
          (throw e))))))
