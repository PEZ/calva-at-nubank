---
name: babashka-tasks
description: "Write idiomatic bb.edn tasks: thin wrappers delegating to scripts/*.clj modules. Supports project-level bb and global bbg tasks. Use when: creating or modifying a bb/bbg task, editing bb.edn or scripts/*.clj files, structuring task modules, adding CLI argument parsing, resource lifecycle management, or agent-friendly output patterns."
---

# Babashka Tasks

Write idiomatic bb.edn tasks: thin declarative wrappers that delegate to well-structured scripts/*.clj modules.

**Prerequisite**: Always read the `babashka` skill before working on tasks. It covers REPL-driven development, REPL-loadable script patterns, shell vs process, data-oriented design, and namespace reference - all foundational to writing good task modules.

## When to invoke

- User asks to "add a bb task" or "create a new task"
- User asks to "add a global task" or mentions `bbg`
- Editing bb.edn or scripts/*.clj files
- Automating a build, dev, or release workflow

## When NOT to invoke

- General Babashka scripting without bb.edn - use the `babashka` skill
- ClojureScript/Squint compilation (project-specific build docs)
- tools.build build.clj files (different API patterns)

## Architecture

```
bb.edn                          scripts/*.clj
  :requires [ns]                   (ns my-module
  task-name {:task (ns/fn args)}     (:require [babashka.fs :as fs]))
                                   (defn my-fn [opts] ...)
```

Tasks are thin. Modules hold logic.

### Project-level vs Global tasks

Babashka tasks exist at two levels:

| Level | Command | bb.edn location | Scripts location | Purpose |
|---|---|---|---|---|
| **Project** | `bb` | `<project>/bb.edn` | `<project>/scripts/*.clj` | Build, test, dev workflows for a specific project |
| **Global** | `bbg` | `~/.config/bbg/bb.edn` | `~/.config/bbg/scripts/*.clj` | Personal utilities available from any directory |

`bbg` is a shell script (`~/bin/bbg`) that `cd`s to `~/.config/bbg/` and runs `bb` with `--cwd` set to the caller's original directory. This means the global `bb.edn` and its scripts are always found regardless of where the user invokes `bbg`.

The same architecture applies to both: thin bb.edn wrappers delegating to scripts/*.clj modules. When creating global tasks, remember:

- `bbg` passes `--cwd "$CALLER_CWD"` so tasks can access the caller's directory
- The bb.edn `:enter` hook can capture it: `(def cwd (:cwd (cli/parse-opts *command-line-args*)))`
- `(babashka.fs/cwd)` and `(System/getProperty "user.dir")` return `~/.config/bbg/`, not the caller's directory

Example global task that operates on the caller's directory:

```clojure
;; ~/.config/bbg/bb.edn - thin wrapper with :enter hook for cwd
{:paths ["scripts"]
 :deps {org.babashka/cli {:mvn/version "0.2.23"}}
 :tasks
 {:requires ([babashka.cli :as cli]
             [loc])

  :enter (def cwd (:cwd (cli/parse-opts *command-line-args*)))

  loc {:doc "Count lines of code"
       :task (loc/count! {:cwd cwd})}}}
```

```clojure
;; ~/.config/bbg/scripts/loc.clj - logic lives here
(ns loc
  (:require [babashka.fs :as fs]))

(defn count! [{:keys [cwd]}]
  (when-not cwd
    (println "Error: no --cwd provided (are you running via bbg?)")
    (System/exit 1))
  (let [files (fs/glob cwd "**/*.clj")]
    (println (format "%4d clj files" (count files)))))
```

Usage: `bbg loc` (cwd is passed automatically by the bbg script)

## bb.edn Structure

```clojure
{:paths ["scripts"]          ;; put modules on classpath
 :deps  {org.babashka/cli {:mvn/version "0.2.23"}}
 :tasks
 {:requires ([babashka.cli :as cli]
             [my-module])    ;; top-level requires shared across tasks

  my-task {:doc "What it does"
           :task (my-module/start! (cli/parse-opts *command-line-args*
                                                  {:coerce {:port :int}}))}

  -private-task {:doc "Internal helper (hidden from bb tasks listing)"
                 :task (do-something)}

  compound-task {:doc "Runs sub-tasks in parallel"
                 :depends [-private-task]
                 :task (run '-compound-all {:parallel true})}
  -compound-all {:depends [-private-task another-task]}}}
```

## Decision Framework

### When to inline vs delegate

| Situation | Pattern |
|---|---|
| Single shell command | Inline: `{:task (p/shell "cmd")}` |
| 2-3 lines, no branching | Inline with `do` |
| Validation, branching, error handling | Delegate to scripts/*.clj |
| Reused across multiple tasks | Always delegate |
| CLI argument parsing beyond simple coerce | Delegate |

## Module Template

```clojure
(ns my-module
  (:require [babashka.cli :as cli]
            [babashka.fs :as fs]
            [babashka.process :as p]
            [clojure.string :as str]))

;; ============================================================
;; Pure helpers (no side effects, defined before callers)
;; ============================================================

(defn- validate-args
  "Gather facts, return {:valid? bool :errors [...] :config {...}}"
  [opts]
  (let [errors (cond-> []
                 (not (:port opts)) (conj "Missing --port")
                 (not (fs/exists? (:dir opts "."))) (conj "Directory not found"))]
    {:valid? (empty? errors)
     :errors errors
     :config (merge {:port 8080} opts)}))

;; ============================================================
;; Side-effecting functions (edges only)
;; ============================================================

(defn start!
  "Entry point called from bb.edn task.
   Gather-then-decide: validate all inputs before acting."
  [opts]
  (let [{:keys [valid? errors config]} (validate-args opts)]
    (if valid?
      (do
        (println (str "Starting on port " (:port config)))
        (p/shell "my-server" "--port" (str (:port config))))
      (do
        (doseq [e errors] (println (str "Error: " e)))
        (System/exit 1)))))
```

## Patterns

### Task-to-module wiring

```clojure
;; bb.edn - thin wrapper passes parsed opts (see babashka skill: CLI argument parsing)
my-task {:task (my-module/start!
                (cli/parse-opts *command-line-args*
                                {:coerce {:port :int :verbose :boolean}
                                 :alias {:p :port :v :verbose}}))}

;; Separating task args from forwarded args (e.g. to Playwright)
;; Use -- to separate: bb my-task --my-flag -- --forwarded-arg
my-task {:task (let [{:keys [args opts]} (cli/parse-args *command-line-args*
                                                         {:coerce {:shards :int}
                                                          :alias {:s :serial}})]
                 (my-module/start! args opts))}
```

### Agent-Friendly Output

Write output to `.tmp/` files so AI agents can read results with `read_file` instead of parsing terminal output. Be sure to mention in the task's `:doc` string, and output, where results will be/are written.

```clojure
(defn- write-output! [filename content]
  (fs/create-dirs ".tmp")
  (spit (str ".tmp/" filename) content))

;; In your task function
(let [result (run-tests!)]
  (write-output! "test-output.txt" (:output result))
  (println "Results written to .tmp/test-output.txt"))
```

## Common mistakes

| Mistake | Correction |
|---|---|
| Putting logic in bb.edn `:task` | Delegate to scripts/*.clj module |
| Missing `:doc` string on task | Always add `:doc` for discoverability |
| Forward declaring functions | Define before use - rearrange file structure |
| Validation interleaved with execution | Gather all facts first, display diagnostics, then act |
| Using `System/exit` inside `with-*` wrappers | Return exit code, call `System/exit` after cleanup |
| Top-level side effects in modules | See the `babashka` skill: REPL-loadable scripts |

## Workflow: Adding a New Task

**This workflow applies to planning AND implementation.** When creating a plan document for a new task, use the REPL to verify API behavior, test glob patterns, and validate assumptions. Don't write a plan full of untested guesses and defer all exploration to the implementer.

1. **REPL gate**: Ensure a bb REPL is available (see `babashka` skill: REPL-driven development)
2. **Check existing tasks**: Read bb.edn to understand conventions
3. **Decide inline vs module**: Simple command? Inline. Logic? Delegate to scripts/*.clj
4. **Explore in the REPL**: Understand the data and APIs you will use
5. **Write the module function**: Pure validation first, side effects at edges
6. **Test pure helpers in REPL**: `(require '[my-module :as m] :reload)` then evaluate
7. **Add bb.edn entry**: Thin wrapper with `:doc` string
8. **Test full task**: Run `bb my-task` to verify end-to-end

## Quality Checklist

- [ ] REPL gate passed (bb session verified or user greenlighted REPL-less)
- [ ] Pure functions explored and validated in REPL before wiring side effects
- [ ] bb.edn entry is thin (just namespace call with args)
- [ ] No forward declares (definition order correct)
- [ ] Validation gathered before side effects
- [ ] Used `babashka.fs` instead of shell commands for file operations
- [ ] Used vector args for `shell`/`process`, not string interpolation
- [ ] Task has `:doc` string
