# Presentation TODO

## Content

- [x] Show that Custom REPL Commands can be configured as keybindings, e.g.:
  ```json
  {
    "key": "ctrl+cmd+u alt+enter",
    "command": "calva.runCustomREPLCommand",
    "args": {
      "ns": "user",
      "snippet": "($current-form)",
      "evaluationSendCodeToOutputWindow": false
    }
  }
  ```
- [x] Calva Connect and Jack-in crash course
- [x] Add Connect Sequences examples (e.g. zero-prompt Jack-in, custom cljsType, multi-connection setup)

- [x] Example config.edn with repl commands
- [x] Epupp project: Starting scittle and squint REPLs. Poor man's REPLs, but way better than nothing.
- [ ] “Get greedy about automation”
- [x] bbg slide