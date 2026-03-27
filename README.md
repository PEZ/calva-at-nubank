# REPL Workflow Automation with Calva and Joyride

Slides for a presentation I made for Nubank devs on interactive development with Calva, and Joyride. It is also a working project for running the slides. Plus more.

## Run the Presentation

0. Clone this repo
1. Install [Calva](https://calva.io) and [Joyride](https://github.com/BetterThanTomorrow/joyride) in VS Code
1. Run `npm install` in the `.joyride/` directory
1. Copy the [keybindings](#keybindings) to your `keybindings.json`
1. Open this workspace in VS Code
1. <kbd>F5</kbd> twice: Will switch to zen and presentation mode showing the first slide.
1. Start the `showtime` timer by clicking on it in the status bar
1. Navigate with arrow keys (left/right)
1. Switch between show and edit mode for a slide by pressing <kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>J</kbd> <kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>M</kbd>.
1. Open a Joyride flare: Click the Flares button in the status bar, select **Open URL in Sidebar...** and submit with `https://calva.io`. Hide the sidebar with <kbd>Cmd/Ctrl</kbd>+<kbd>B</kbd>.

The slide order is defined in [slides.edn](slides.edn). The styling lives in [next-slide.css](next-slide.css). The navigation logic is in [.joyride/src/prezo/next_slide.cljs](.joyride/src/prezo/next_slide.cljs). There's a dedicated [next-slide-editor skill](.github/skills/next-slide-editor/SKILL.md) for adding and updating slides.

## What's in this Box

- **A custom slide deck**: markdown rendered in VS Code with Joyride-powered navigation
- **3 runnable projects**: a Clojure language playground, a toy full-stack ClojureScript app, and an interactive Scittle game
- **6 REPL connections**: different Clojure/ClojureScript/Babashka runtimes working simultaneously
- **Custom REPL commands**: bundled in the workspace settings for testing, Epupp userscript upload, dependency management, and more
- **Joyride automation**: the presentation system itself, WebView panels (Flares), browser tampering, a keybinding palette, and more
- **Copilot customization**: agents, skills, and custom instructions relevant for the content of this project
- **Babashka Automation recipe**: global Babashka tasks (bbg)
- **Babashka `bb-nrepl` task** to start a bb repl in a Calva Connect friendly way
- **Local Babashka `localize`/`globalize` sync tasks** that I created to pack some of my global scripts into the project. You can use `globalize` to get the scripts and config from the project to your user config.

## Prerequisites

Depending on what you want to play with.

- **Java**: the only hard requirement for the Clojure projects
- **VS Code** with [Calva](https://calva.io) and [Joyride](https://github.com/BetterThanTomorrow/joyride)
- **Node.js**: for Joyride scripting and the automation recipes
- **Docker**: for the containerized pirate-lang REPL
- **Babashka** (`bb`): for workspace tasks (`bb localize`, `bb globalize`)
- [Backseat Driver](https://github.com/BetterThanTomorrow/backseat-driver) extension: for Copilot + Calva REPL integration

## The Six REPL Connections

When you open the Calva REPL menu, you get five custom [Calva Connect Sequences](https://calva.io/connect-sequences/) plus Calva's built-in Babashka sequence. Three use **Jack-in** (Calva starts the REPL for you) and three use **Connect** (you start the REPL first, then Calva connects to it).

The custom sequences are defined in [settings](.vscode/settings.json).

| Sequence | Mode | Runtime | Use case | Ports |
|---|---|---|---|---|
| **Shadow fullstack** | Jack-in | JVM + shadow-cljs (browser) | Full-stack development: backend + ClojureScript frontend | auto |
| **pirate-lang** | Jack-in | JVM Clojure (local) | Language experiments, no Docker needed | auto |
| **Docker REPL (pirate-lang)** | Jack-in | JVM Clojure (containerized) | Same project, isolated in Docker (custom Jack-in command) | 7888 |
| **Epupp REPL** | Connect | Scittle (browser SCI) | Browser tampering, live demos on calva.io | 3339 / 3340 |
| **Scittle Tic-Tac-Toe REPL** | Connect | Scittle (browser SCI) | Replicant tic-tac-toe game in a Joyride Flare | 1339 / 1340 |
| **Babashka REPL** | Connect | Babashka (built-in sequence) | Workspace scripting and tasks | auto (`bb/.nrepl-port`) |

The three **Connect** sequences require the background REPLs to be running first. Run the **Start Dev Environment** build task (<kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>B</kbd>) to start all three.

You can have multiple connections open simultaneously. Calva routes files to the right REPL automatically. (Plus you can pin a session.) With Calva Backseat Driver the AI agent has tools to list the sequences.

## The Projects

### pirate-lang: Clojure Language Playground

A super silly one-file Clojure project, with a Docker option for isolated development.

**Start it:**
* Open [pirate_lang.clj](projects/pirate-lang/src/pez/pirate_lang.clj)
* From the Command Palette: **Start your project with a REPL and connect (a.k.a. Jack-in)**
* Select **pirate-lang**

The repl-session `pirate-lang` should show as active in the status bar.

**What to try:**
- Evaluate some forms

There's not much more to it. 😀

### pirate-lang: Dockerized

Same project, isolated in a container. Exposes nREPL on port 7888. Source, test, and resource directories are mounted as volumes so edits are live.

The connect sequence uses a [custom Jack-in command line](https://calva.io/connect-sequences/#custom-command-line) (`bb docker-repl`) that builds and runs the container automatically. Requires Docker.

**Start it:**
* Open [pirate_lang.clj](projects/pirate-lang/src/pez/pirate_lang.clj)
* From the Command Palette: **Start your project with a REPL and connect (a.k.a. Jack-in)**
* Select **Docker REPL (pirate-lang)**

> [!NOTE]
> The REPL session is named `pirate-lang-docker`, if the status bar still says `pairate-lang` it is because you still have that repl connected. Since it is targeting the same files, the auto-route doesn't know which one, but you can manually select the session to target by clicking the session button in the status bar and pinning a session.

### shadow-w-backend: Mini Fullstack App

A tiny but complete full-stack app following Thomas Heller's [Fullstack Workflow with shadow-cljs](https://code.thheller.com/blog/shadow-cljs/2024/10/18/fullstack-cljs-workflow-with-shadow-cljs.html). Clojure backend + ClojureScript frontend, hot-reloading on both sides.

**Start it:**
* Open [repl.clj](projects/shadow-w-backend/src/dev/repl.clj)
1. Jack-in: (<kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>C</kbd> <kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>J</kbd>), then select **Shadow fullstack**
2. Load the file, and evaluate `(go!)` in the Rich Comment to start the backend
3. The frontend should be started at [localhost:3000](http://localhost:3000). The most fun way to do it is to click the **Flares** button in the status bar -> **Open URL in Sidebar...** -> Type `http://localhost:3000` and submit. The Flare manager script is in [.joyride/src/flares.cljs](.joyride/src/flares.cljs).

**What to try:**
- Evaluate stuff in [server.clj](projects/shadow-w-backend/src/main/acme/server.clj) (backend) or [app.cljs](projects/shadow-w-backend/src/main/acme/frontend/app.cljs) (e.g.)

The project has its own [README](projects/shadow-w-backend/README.md): read it for the full (very short) story. Java is the only requirement.

### Scittle Tic-Tac-Toe: Interactive Browser Game

A tic-tac-toe game built with Scittle and [Replicant](https://github.com/cjohansen/replicant). Runs in a Joyride Flare (WebView panel).

**Start it:**
1. Run the **Start Dev Environment** build task if not already running (<kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>B</kbd>)
2. Open the Calva REPL menu, pick **Connect to a running REPL in your project**, then select **Scittle Tic-Tac-Toe REPL**
3. The game runs in a Joyride Flare (WebView panel in the sidebar)

The source lives in [.joyride/src/scittle-replicant-tic-tac-toe/](.joyride/src/scittle-replicant-tic-tac-toe/).

## Joyride

Because reasons the Joyride REPL can't be started by VS Code Tasks. But you really should start it.
* From the command palette, search: **Start joy**.

It will find the right command for you (**Calva: Start Joyride REPL and Connect**).

Then explore [live_examples.cljs](.joyride/src/live_examples.cljs): assorted Joyride patterns (status bar items, information messages, VS Code API demos). It is designed so that you can evaluate one form at a time. E.g. You can use ParEdit to move forward one form at a time and evaluate top level form at each.

1. <kbd>Ctrl</kbd>+<kbd>right</kbd> (Win/Linux) / <kbd>Alt</kbd>+<kbd>right</kbd> (Mac)
2. <kbd>Alt</kbd>+<kbd>Enter</kbd>
3. Repeat

**Tip**: This project is configured to hide line numbers. In [live_examples.cljs](.joyride/src/live_examples.cljs) there's a form for toggling them on and off. There's also a keybinding (<kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>J</kbd> <kbd>L</kbd>) and a custom REPL command (`l`) configured for it. The keybinding uses `joyride.runCode` so it works without an nREPL connection — the REPL command requires the Joyride REPL to be connected.

### Next Slide, Please

This entire project is also a Joyride showcase. The presentation system, the demo tooling, and several general-purpose scripts are all Joyride code.

#### Presentation Infrastructure

- [next_slide.cljs](.joyride/src/prezo/next_slide.cljs): slide navigator with keyboard shortcuts (arrow keys, page up/down)
- [next_slide_notes.cljs](.joyride/src/prezo/next_slide_notes.cljs): speaker notes companion
- [showtime.cljs](.joyride/src/showtime.cljs): status bar timer for tracking presentation time (click to start -> click to stop -> click to restart)

#### Flares (WebView Panels)

- [flares.cljs](.joyride/src/flares.cljs): URL picker and sidebar slot manager with history, keybinding-driven
- [flares_examples.cljs](.joyride/src/flares_examples.cljs): demos: Fibonacci sequences, animated SVG orbits, sidebar panels, Hiccup and HTML rendering
- [assets/example-flare.edn](assets/example-flare.edn): an EDN Flare with embedded Scittle

#### The Missing Command Palette

- [keybinding_palette.cljs](.joyride/scripts/keybinding_palette.cljs): a command palette for your keybindings (reads `keybindings.json`, shows a searchable picker, uses an npm module for parsing JSONC).

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
  "key": "ctrl+alt+j ctrl+alt+m",
  "command": "markdown.showPreview"
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

// Toggle line numbers (works in Zen Mode)
{
  "key": "ctrl+alt+j l",
  "command": "joyride.runCode",
  "args": "(set! (.-lineNumbers vscode/window.activeTextEditor.options) ({1 0 0 1} (.-lineNumbers vscode/window.activeTextEditor.options)))"
},

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
3. Open the Calva REPL menu, pick **Connect to a running REPL in your project**, then select **Epupp REPL**
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
