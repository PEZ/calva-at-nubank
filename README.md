# REPL Workflow Automation with Calva and Joyride

A companion repository for a presentation I made for Nubank devs on interactive development with Calva, and Joyride. This is the slide deck. It is also a working project for running the slides. There are also has embedded demo projects, preconfigure Calva Connect Sequences, and some automation patterns I am fond of. Plus:

- **A custom slide deck**: markdown rendered in VS Code with Joyride-powered navigation
- **3 runnable projects**: a Clojure language playground, a toy full-stack ClojureScript app, and an interactive Scittle game
- **5 REPL connections**: different Clojure/ClojureScript runtimes working simultaneously
- **Joyride automation**: the presentation system itself, WebView panels (Flares), browser tampering, a keybinding palette, and more
- **Automation recipes**: global Babashka tasks (bbg) and a workspace/global sync system. That I created to pack some of my global scripts into the project. You can use it to get the scripts out of the project. 😀
- **Copilot customization**: agents, skills, and custom instructions applied to real workflows

## Prerequisites

Depending on what you want to play with.

- **Java**: the only hard requirement for the Clojure projects
- **VS Code** with [Calva](https://calva.io): for REPL support, formatting, and the slide system
- **Node.js**: for Joyride scripting and the automation recipes. After cloning, run `npm install` in the `.joyride/` directory to install the npm packages that some Joyride scripts depend on (the keybinding palette and some live examples).
- **Babashka** (`bb`): for workspace tasks (`bb localize`, `bb globalize`)
- **Docker**: for the containerized pirate-lang REPL
- [Joyride](https://github.com/BetterThanTomorrow/joyride) extension: for VS Code scripting (the presentation system, Flares, automation)
- [Backseat Driver](https://github.com/BetterThanTomorrow/backseat-driver) extension: for Copilot + Calva REPL integration

## Running the Presentation

The slides are markdown files styled with custom CSS, rendered in VS Code's markdown preview. Joyride provides keyboard navigation.

1. Open this workspace in VS Code
2. Run the **Start Dev Environment** build task (<kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>B</kbd>): this launches the Scittle and Epupp background REPLs that power some demos
3. Open [slides/hello.md](slides/hello.md) and toggle markdown preview (<kbd>Cmd</kbd>+<kbd>Shift</kbd>+<kbd>V</kbd>)
4. Navigate with arrow keys (left/right) when the slide navigator is active

The slide order is defined in [slides.edn](slides.edn). The styling lives in [next-slide.css](next-slide.css). The navigation logic is in [.joyride/src/prezo/next_slide.cljs](.joyride/src/prezo/next_slide.cljs). There's a dedicated [next-slide-editor skill](.github/skills/next-slide-editor/SKILL.md) for adding and updating slides.

## The Five REPL Connections

When you open the Calva REPL menu, you get five pre-configured  [Calva Connect Sequences](https://calva.io/connect-sequences/). Three use **Jack-in** (Calva starts the REPL for you) and two use **Connect** (you start the REPL first, then Calva connects to it).

You find the sequences in [settings](.vscode/settings.json).

| Sequence | Mode | Runtime | Use case | Ports |
|---|---|---|---|---|
| **Shadow fullstack** | Jack-in | JVM + shadow-cljs (browser) | Full-stack development: backend + ClojureScript frontend | auto |
| **pirate-lang** | Jack-in | JVM Clojure (local) | Language experiments, no Docker needed | auto |
| **Docker REPL (pirate-lang)** | Jack-in | JVM Clojure (containerized) | Same project, isolated in Docker (custom Jack-in command) | 7888 |
| **Epupp REPL** | Connect | Scittle (browser SCI) | Browser tampering, live demos on calva.io | 3339 / 3340 |
| **Scittle Tic-Tac-Toe REPL** | Connect | Scittle (browser SCI) | Replicant tic-tac-toe game in a Joyride Flare | 1339 / 1340 |

The two **Connect** sequences require the background REPLs to be running first. Run the **Start Dev Environment** build task (<kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>B</kbd>) to start both.

You can have multiple connections open simultaneously. Calva routes files to the right REPL automatically. (Plus you can pin a session.) With Calva Backseat Driver the AI agent has tools to list the sequences.

All sequences are defined in [.vscode/settings.json](.vscode/settings.json) under `calva.replConnectSequences`.

## The Projects

### pirate-lang: Clojure Language Playground

A super silly one-file Clojure project, with a Docker option for isolated development.

**Start it:** Calva Jack-in -> **pirate-lang**. For the containerized version: Calva Jack-in -> **Docker REPL (pirate-lang)** (requires Docker).

**What to try:**
- Open [projects/pirate-lang/src/pez/pirate_lang.clj](projects/pirate-lang/src/pez/pirate_lang.clj) and evaluate forms
- Run tests from the REPL
- Explore the fiddles in [projects/pirate-lang/dev/fiddles/](projects/pirate-lang/dev/fiddles/)

### shadow-w-backend: Mini Fullstack App

A tiny but complete full-stack app following Thomas Heller's [Fullstack Workflow with shadow-cljs](https://code.thheller.com/blog/shadow-cljs/2024/10/18/fullstack-cljs-workflow-with-shadow-cljs.html). Clojure backend + ClojureScript frontend, hot-reloading on both sides.

**Start it:**
1. Calva Jack-in -> **Shadow fullstack**
2. In the `backend` REPL session, evaluate `(go!)` to start the Jetty server
3. Open [localhost:3000](http://localhost:3000)

**What to try:**
- Edit [server.clj](projects/shadow-w-backend/src/main/acme/server.clj) (backend) or [app.cljs](projects/shadow-w-backend/src/main/acme/frontend/app.cljs) (frontend) and see live changes
- Use the custom REPL command to reload Clojure code and restart the server

The project has its own detailed [README](projects/shadow-w-backend/README.md): read it for the full story. Java is the only requirement.

### Scittle Tic-Tac-Toe: Interactive Browser Game

A tic-tac-toe game built with Scittle and [Replicant](https://github.com/cjohansen/replicant). Runs in a Joyride Flare (WebView panel).

**Start it:**
1. Run the **Start Dev Environment** build task if not already running (<kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>B</kbd>)
2. Calva Connect -> **Scittle Tic-Tac-Toe REPL**
3. The game runs in a Joyride Flare (WebView panel in the sidebar)

The source lives in [.joyride/src/scittle-replicant-tic-tac-toe/](.joyride/src/scittle-replicant-tic-tac-toe/).

## Joyride Automation

This entire project is also a Joyride showcase. The presentation system, the demo tooling, and several general-purpose scripts are all Joyride code.

### Presentation Infrastructure

- [next_slide.cljs](.joyride/src/prezo/next_slide.cljs): slide navigator with keyboard shortcuts (arrow keys, page up/down)
- [next_slide_notes.cljs](.joyride/src/prezo/next_slide_notes.cljs): speaker notes companion
- [showtime.cljs](.joyride/src/showtime.cljs): status bar timer for tracking presentation time

### Flares (WebView Panels)

- [flares.cljs](.joyride/src/flares.cljs): URL picker and sidebar slot manager with history, keybinding-driven
- [flares_examples.cljs](.joyride/src/flares_examples.cljs): demos: Fibonacci sequences, animated SVG orbits, sidebar panels, Hiccup and HTML rendering
- [assets/example-flare.edn](assets/example-flare.edn): an EDN Flare with embedded Scittle

### General-Purpose Scripts

- [keybinding_palette.cljs](.joyride/scripts/keybinding_palette.cljs): a command palette for your keybindings (reads `keybindings.json`, shows a searchable picker)
- [live_examples.cljs](.joyride/src/live_examples.cljs): assorted Joyride patterns (status bar items, information messages, VS Code API demos)

### Extending with Copilot

There are [Joyride-specific skills](.github/skills/joyride/SKILL.md) for general Joyride development. And an example Copilot prompt for extending the slide system at [.github/prompts/Implement.prompt.md](.github/prompts/Implement.prompt.md).

## Keybindings

The slide navigator is activated automatically when the workspace opens (via [workspace_activate.cljs](.joyride/scripts/workspace_activate.cljs)). Use <kbd>F5</kbd> to enter Zen Mode for a clean presentation view. Arrow keys and Page Up/Down navigate between slides. Also works with a Clicker. <kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>Cmd</kbd>+<kbd>Left</kbd> restarts from the first slide.

For this (and more) to work you need to copy these keybindings to your `keybindings.json`:

```json
//// BEGIN DEMO BINDINGS ////

// Slide Navigation
{
  "key": "ctrl+alt+j s",
  "command": "joyride.runCode",
  "args": "(prezo.next-slide/activate!)"
},
{
  "key": "ctrl+alt+j ctrl+alt+s",
  "command": "joyride.runCode",
  "args": "(prezo.next-slide/deactivate!)"
},
{
  "key": "right",
  "command": "joyride.runCode",
  "args": "(prezo.next-slide/next! true)",
  "when": "next-slide:active && !inputFocus"
},
{
  "key": "left",
  "command": "joyride.runCode",
  "args": "(prezo.next-slide/next! false)",
  "when": "next-slide:active && !inputFocus"
},
{
  "key": "pagedown",
  "command": "joyride.runCode",
  "args": "(prezo.next-slide/next! true)",
  "when": "next-slide:active"
},
{
  "key": "pageup",
  "command": "joyride.runCode",
  "args": "(prezo.next-slide/next! false)",
  "when": "next-slide:active"
},
{
  "key": "F5",
  "command": "workbench.action.toggleZenMode",
  "when": "next-slide:active && !inZenMode"
},
{
  "key": "F5",
  "command": "joyride.runCode",
  "args": "(prezo.next-slide/current!)",
  "when": "next-slide:active && inZenMode"
},
{
  "key": "ctrl+alt+cmd+left",
  "command": "joyride.runCode",
  "args": "(prezo.next-slide/restart!)"
}

// Flares and Utilities
{
  "key": "ctrl+alt+j ctrl+shift+f",
  "command": "joyride.runCode",
  "args": "(do (require '[flares] :reload) (flares/show-flares-picker!+))"
},
{
  "key": "ctrl+alt+j ctrl+alt+b",
  "command": "joyride.runCode",
  "args": "(do (require '[flares] :reload) (flares/prompt-and-open-url-in-sidebar!+))"
},
{
  "key": "ctrl+alt+j alt+b",
  "command": "joyride.runCode",
  "args": "(do (require '[flares] :reload) (flares/prompt-and-open-url-as-panel!+))"
},
{
  "key": "ctrl+alt+j ctrl+alt+j",
  "command": "joyride.runCode",
  "args": "(require '[keybinding-palette :as kp] :reload) (kp/show-palette!+)"
}

//// END DEMO BINDINGS ////
```

The last one opens the **keybinding palette**: a searchable picker that shows all your keybindings with descriptions. Handy for discovering what's available.

## Epupp: Browser Tampering

[Epupp](https://github.com/PEZ/epupp) connects a ClojureScript REPL to web pages in your browser, letting you modify them live.

### Live Demo Helpers

[live-tampers/calva_io.cljs](live-tampers/calva_io.cljs) has functions for demoing Epupp on [calva.io](https://calva.io):

- `(search! "jack-in")`: opens the MkDocs search overlay
- `(go! 0)`: navigates to a search result
- `(toggle-dark-mode!)`: toggles a CSS dark mode injection
- `(navigate! "https://calva.io/")`: navigates to any URL

### Userscripts

[epupp-userscripts/pez/calva_io_darkmode.cljs](epupp-userscripts/pez/calva_io_darkmode.cljs): an auto-running dark mode for calva.io. It uses Epupp's metadata system (`{:epupp/auto-run-match "https://calva.io*"}`) to activate automatically.

**Try it:**
1. Run the **Start Dev Environment** build task if not already running (<kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>B</kbd>)
2. Open calva.io in a browser with the [Epupp extension](https://github.com/AlfredoProgworx/epupp) and connect it to port 3340
3. Calva Connect -> **Epupp REPL**
4. Open [live-tampers/calva_io.cljs](live-tampers/calva_io.cljs) and evaluate forms against the page

## Automation Recipes

### Global Babashka Tasks (bbg)

**bbg** is a pattern for running Babashka tasks from any directory. Put a `bb.edn` + scripts in `~/.config/bbg/`, symlink a wrapper to `~/bin/bbg`, and you get project-independent automation with shell completions.

The wrapper script:

```bash
#!/usr/bin/env bash
CALLER_CWD="$(pwd)"
cd ~/.config/bbg
bb "$@" --cwd "$CALLER_CWD"
```

Tasks get access to both their own resources (in `~/.config/bbg/`) and the caller's working directory via `--cwd`. Add zsh completions for tab completion of task names and options.

The [slides/bbg.md](slides/bbg.md) slide covers the full recipe. For a working example with several tasks (including an `mdq` Markdown query tool), see also my personal bbg tasks: [github.com/PEZ/my-bbg](https://github.com/PEZ/my-bbg).

### Workspace/Global Sync (bb localize / bb globalize)

This workspace uses `bb localize` and `bb globalize` to keep Joyride scripts and Copilot customization files in sync between the workspace and your global user configuration.

```bash
bb localize    # Copy from global ~/.config/joyride/ and ~/.copilot/ into this workspace
bb globalize   # Copy from this workspace back to global
```

What gets synced is defined in [dependencies-sync.edn](dependencies-sync.edn): Joyride source files, npm deps, Copilot skills, agents, and prompts. The logic lives in [scripts/dependencies_sync.clj](scripts/dependencies_sync.clj).

The idea: develop and refine your automation in one workspace, then `globalize` to make it your default. Next project, `localize` pulls it in. You could imagine extending this to bbg tasks too.

## Copilot & AI

The workspace includes Copilot customization as working examples:

- [AGENTS.md](AGENTS.md): workspace-level agent operating principles
- [.github/agents/](.github/agents/): custom agent modes (Clojure editor, interactive programming, reviewer)
- [.github/skills/](.github/skills/): domain skills (Babashka, Joyride, Epupp, next-slide-editor, and more)
- [.github/prompts/](.github/prompts/): reusable prompts

[Backseat Driver](https://github.com/BetterThanTomorrow/backseat-driver) integrates Copilot with Calva's REPL: the AI can evaluate code, do structural editing, and look up symbols. Install the extension and it works.


### Resources

- [CalvaTV](https://www.youtube.com/@CalvaTV): recorded demos and tutorials
- [calva.io](https://calva.io): Calva documentation
- [Joyride](https://github.com/BetterThanTomorrow/joyride): VS Code scripting in ClojureScript
- [Backseat Driver](https://github.com/BetterThanTomorrow/backseat-driver): Gives Copilot Calva tools, including the REPL
- [PEZ/my-bbg](https://github.com/PEZ/my-bbg): My personal `bbg` global tasks repo
- [Replicant](https://github.com/cjohansen/replicant): pure ClojureScript VDOM rendering

### Happy coding! ❤️
