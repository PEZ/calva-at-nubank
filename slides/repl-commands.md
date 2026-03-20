<div class="slide cols-5-7">

# Custom REPL Commands

<div class="col">

```json
{
  "name": "(clojure.test/run-tests)",
  "key": "r",
  "snippet": "(require 'clojure.test) (clojure.test/run-tests)"
},

{
  "key": "a",
  "name": "Add selected dependency/ies",
  "snippet": "(require '[clojure.repl.deps :refer [add-libs]])\\n(add-libs '{$selection})"
},

{
  "name": "Upload current Epupp userscript",
  "snippet": "(epupp.fs/save! ${file-text|pr-str} {:fs/force? true})"
},
```

#### Keybindings

```json

// Malli: Infer schema from data
  {
    "key": "ctrl+alt+m ctrl+alt+enter",
    "command": "calva.runCustomREPLCommand",
    "args": {
      "snippet": "(require '[malli.provider :as mp]) (mp/provide $top-level-form)"
    }
  },

// RCF: Run inline tests
  {
    "key": "ctrl+alt+c ctrl+alt+enter",
    "when": "calva:connected",
    "command": "runCommands",
    "args": {
      "commands": [
        {
          "command": "calva.runCustomREPLCommand",
          "args": "(clojure.core/refer-clojure) (require 'hyperfiddle.rcf) (hyperfiddle.rcf/enable! true) (println \"rcf tests enabled\")"
        },
        {
          "command": "calva.loadFile"
        },
        {
          "command": "calva.runCustomREPLCommand",
          "args": "(hyperfiddle.rcf/enable! false) (println \"rcf tests disabled\") *3"
        }
      ]
    }
  },

  // As-json-markdown-block
  {
    "key": "shift+ctrl+alt+n",
    // "when": "editorTextFocus && editorHasSelection",
    "command": "runCommands",
    "args": {
      "commands": [
        "editor.action.clipboardCopyAction",
        {
          "command": "workbench.action.files.newUntitledFile",
          "args": {
            "languageId": "json"
          }
        },
        {
          "command": "editor.action.insertSnippet",
          "args": {
            "snippet": "${1:$CLIPBOARD}"
          }
        },
        "editor.action.formatSelection",
        "editor.action.clipboardCutAction",
        {
          "command": "editor.action.insertSnippet",
          "args": {
            "snippet": "${1:``` json\n$CLIPBOARD\n```}"
          }
        },
      ]
    }
  },
```

</div>

<div class="col">

#### Substitution variables

- **Forms**: `$current-form`, `$top-level-form`, `$enclosing-form`
- **Symbols**: `$current-fn`, `$top-level-defined-symbol`
- **Context**: `$ns`, `$editor-ns`, `$selection`, `$file`
- **Cursor**: `$head`, `$tail`, `$line`, `$column`
- **Modifiers**: `${variable|modifier|args}` - `pr-str`, `replace`, `replace-first`

#### Configuration sources (merged)

1. VS Code User settings
2. VS Code Workspace settings
3. `.calva/config.edn` (auto-refreshed!)
4. `~/.config/calva/config.edn`
5. Snippets inside deps (library authors ship in `calva.exports/config.edn`)

#### Hover snippets

- `calva.customREPLHoverSnippets`- eval on tooltip
- Same substitutions + `$hover-text` and `$hover-*` variants

```json
  "calva.customREPLHoverSnippets": [
    {
      "name": "Eval text on hover",
      "snippet": "(str :Hello_Nubank! \" $hover-text\")"
    }
  ],
```

</div>

</div>
