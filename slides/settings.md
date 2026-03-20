<div class="slide cols" style="--cols: 1fr 4fr">

# Settings, where?

<div class="col">

- VS Code: `settings.json`, `*.code-workspace`, `keybindings.json`, `tasks.json`
- Calva:
  -  VS Code
  - `.calva/config.edn`, `~/.config/calva/config.edn`, `<classpath>/<lib>.jar/calva.exports/config.edn`
  - `cljfmt.edn` (*)
- nREPL: `nrepl.edn`
- REPL: `deps.edn`, `project.clj`, `bb.edn`, `squint.edn`, `nbb.edn`, ...
- clojure-lsp: `.lsp/config.edn`
- clj-kondo: `.clj-kondo/config.edn`
- cljfmt: [`cljfmt.edn`](../cljfmt.edn), `.cljfmt.edn`, `.cljfmt` (*!)

Today:
- Calva formatter and indenter (briefly)
- Calva REPL Connect Sequences
- Calva REPL Commands

</div>
<div class="col">

![Calva Composition](../images/calva-architectural-overview.png)

</div>
</div>
