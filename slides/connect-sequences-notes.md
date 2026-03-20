Demos:

* Add session name to pirate-lang sequence
* Connect pirate-lang
* Connect Shadow fullstack
* mini-shadow-w-backend project

# Connect Sequences

```json
  {
    "command": "calva.connect",
    "args": {
      "disableAutoSelect": true
    },
    "key": "ctrl+alt+c shift+c"
  },
  {
    "command": "calva.jackIn",
    "args": {
      "disableAutoSelect": true
    },
    "key": "ctrl+alt+c shift+j"
  },
```

**`cljsType`** in a connect sequence can be one of these **built-in values**:

| Value | What it does |
| --- | --- |
| `"Figwheel Main"` | Hot-reloading via Figwheel Main |
| `"lein-figwheel"` | Legacy lein-figwheel |
| `"shadow-cljs"` | shadow-cljs managed CLJS REPL |
| `"ClojureScript built-in for browser"` | `cljs.main` browser REPL |
| `"ClojureScript built-in for node"` | `cljs.main` node REPL |
| `"none"` | Skip CLJS connection entirely |

Or it can be a **custom dictionary** with fields like:

- `dependsOn` - which built-in's dependencies to inject (or `"User provided"`)
- `startCode` / `connectCode` - Clojure code to start and connect the CLJS REPL
- `isStarted` - skip the start step (e.g. shadow-cljs already running)
- `isReadyToStartRegExp` / `isConnectedRegExp` - patterns Calva watches in stdout to know when to proceed
- `openUrlRegExp` / `shouldOpenUrl` - auto-open the app URL
- `buildsRequired` - whether builds must be selected

\newpage