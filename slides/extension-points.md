<div class="slide cols-2">

# Calva Extension Points

<div class="col">

**All commands are API** - accept args from keybindings, **Joyride**, &amp; extensions

#### Custom REPL Commands

- Configurable snippets with substitution variables, keyboard shortcuts, hover tooltips

#### Connect Sequences

- **Custom project types** - extend built-in sequences
- **After-connect code** - evaluate on REPL connect

#### Flares

- Show HTML, SVG, or URLs in WebView panels
- [flares.clj](../etc/flares.clj)

</div>
<div class="col">

#### The Calva Extension API

- `repl.evaluateCode()`- target any session, with output handlers
- `repl.listSessions()`- inspect all registered sessions
- `repl.onOutputLogged()`- subscribe to REPL output events
- `ranges.*`- Query where things are, copy the text, or use with ...
- `editor.replace()`- programmatic Clojure document editing
- `document.getNamespace()`
- `pprint.prettyPrint()`- pretty print using Calva's zprint engine

Consumers: **Joyride** code, extensions (e.g. **Calva Backseat Driver**)
</div>

</div>
