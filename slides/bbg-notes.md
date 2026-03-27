## bbg — Notes

- `bbg` is a tiny shell script: `bb --config ~/.config/bbg/bb.edn "$@"`
- `--config` tells bb where to find `bb.edn`; relative paths in `:paths` resolve from there
- Internal tasks (nrepl, watchers, tests) use a `-bbg:` prefix to stay hidden from `bbg tasks`
- Zsh tab completion gives you the same `bb <tab>` experience for global tasks
- Same completion pattern works for project-level `bb` tasks (also in `.zshrc`)
- Skills: `babashka-tasks` covers the architecture, `babashka` covers REPL-driven script development

\newpage