## bbg — Notes

- `bbg` is a tiny shell script that `cd`s to `~/.config/bbg/` and runs `bb` with `--cwd` set to the caller's directory
- This means `bb.edn` and scripts are always found, but tasks can still operate on the caller's working directory
- The `:enter` hook captures cwd: `(def cwd (:cwd (cli/parse-opts *command-line-args*)))`
- `(babashka.fs/cwd)` inside a bbg task returns `~/.config/bbg/`, NOT the caller's directory — use the passed `cwd` instead
- Zsh tab completion gives you the same `bb <tab>` experience for global tasks
- Same completion pattern works for project-level `bb` tasks (also in `.zshrc`)
- Skills: `babashka-tasks` covers the architecture, `babashka` covers REPL-driven script development

\newpage