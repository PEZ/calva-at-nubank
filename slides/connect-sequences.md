<div class="slide cols-2">

# Connect Sequences

<div class="col">

#### `(~= Connect-Sequence Project-Type) => true`

- **Built in project types/sequences** - deps.edn, Leiningen, shadow-cljs, Babashka, Joyride, scittle, nbb, Basilisp ...
- Custom sequences “inherit” built-ins
- Calva prompts to fill in the missing required pieces
- Provide via settings or keyboard shortcuts

#### Among other things...

- **Project root** - for monorepos, multi-project or multi-folder workspaces
- Aliases, Profiles, CLJS builds...
- **ClojureScript type** - Figwheel, shadow-cljs, ClojureScript built-ins node/browser, custom
  - Clojure REPL things, start builds, connect builds, ...
- **Extra middleware** - `extraNReplMiddleware`
- **Custom command line** - fully replace the Jack-in command
- **After-connect code** - evaluate on REPL connect

</div>
<div class="col">

#### Zero-prompt

- `autoSelectForJackIn` / `autoSelectForConnect`
- `menuSelections` pre-fills aliases & builds
- `afterPrimaryReplConnectedCode` to start servers and things
- One keystroke → full-stack REPL
- `"calva.autoStartRepl": true,` Open workspace → full-stack REPL
- `"disableAutoSelect"` in shortcut config to force prompting
#### Multiple connections

- Named sessions (`replSessionNames`)
- File-pattern auto-routing (`replSessionFilePatterns`)
- Select CLJC target per connection
- Pin a sessions to disable auto-routing
- Per-connection disconnect

</div>

<br><br>
**As Keybindings**

```json
  {
    "title": "Connect to REPL (Manual)",
    "category": "Repl",
    "command": "calva.connect",
    "args": {
      "disableAutoSelect": true
    },
    "key": "ctrl+alt+c shift+c"
  },

  {
    "title": "Jack-in (Manual)",
    "category": "Repl",
    "command": "calva.jackIn",
    "args": {
      "disableAutoSelect": true
    },
    "key": "ctrl+alt+c shift+j"
  },

  {
    "title": "Jack-in pirate-lang by sequence name",
    "category": "demo",
    "command": "calva.jackIn",
    "args": {
      "connectSequence": "pirate-lang",
    },
    "key": "ctrl+alt+c alt+p"
  },

  {
    "title": "Jack-in pirate-lang full sequence",
    "category": "demo",
    "command": "calva.jackIn",
    "args": {
      "connectSequence": {
        "projectType": "deps.edn",
        "projectRootPath": [
          "projects",
          "pirate-lang"
        ],
        "cljsType": "none",
        "replSessionNames": {
          "primary": "pirate-lang"
        },
        "menuSelections": {
          "cljAliases": [
            "allow-attach-self",
            "dev",
            "test"
          ]
        }
      },
    },
    "key": "ctrl+alt+c shift+p"
  },
```


</div>
