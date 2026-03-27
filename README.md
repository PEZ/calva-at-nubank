# REPL Workflow Automation with Calva and Joyride

Slides for a presentation I made for Nubank devs on interactive development with Calva, and Joyride. It is also a working project for running the slides. Plus more.

## Run the Presentation

0. Clone this repo
1. Install [Calva](https://calva.io) and [Joyride](https://github.com/BetterThanTomorrow/joyride) in VS Code
1. Run `npm install` in the `.joyride/` directory
1. Open this workspace in VS Code
   * You should see a message: *next-slide activated*
   * You should see buttons for Flares (a flame icon) and a `00:00` (the `showtime` timer) in the status abar.
1. Copy the [keybindings](#keybindings) to your `keybindings.json`
1. <kbd>F5</kbd> twice: Will switch to zen and presentation mode showing the first slide. (Or **Home** on your clicker.)
1. Start the `showtime` timer by clicking on it in the status bar
1. Navigate with arrow keys (left/right)
1. Switch between show and edit mode for a slide by pressing <kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>J</kbd> <kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>M</kbd>.
1. Open a Joyride flare: Click the Flares button in the status bar, select **Open URL in Sidebar...** and submit with `https://calva.io`.
   * Hide the sidebar with <kbd>Cmd/Ctrl</kbd>+<kbd>B</kbd>.

The slide order is defined in [slides.edn](slides.edn). The styling lives in [next-slide.css](next-slide.css). The navigation logic is in [.joyride/src/prezo/next_slide.cljs](.joyride/src/prezo/next_slide.cljs). The workspace has a dedicated [next-slide-editor skill](.github/skills/next-slide-editor/SKILL.md) for getting Copilot help with adding and updating slides.

## What's in this Box

- **A custom slide deck**: markdown rendered in VS Code with Joyride-powered navigation
- **3 runnable (toy) projects**: a Clojure language playground, a full-stack Clojure+ClojureScript app, and an interactive Scittle game
- **5 Custom REPL Connect Sequences**: Together with **2 Calva built-in sequences** you'll get **7 simultaneous REPL connections** with different Clojure/ClojureScript/Babashka/Joyride runtimes working simultaneously
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
- [Epupp](https://github.com/PEZ/epupp) browser extension ([Chrome](https://chromewebstore.google.com/detail/bfcbpnmgefiblppimmoncoflmcejdbei), [Firefox](https://addons.mozilla.org/firefox/addon/epupp/)): for browser tampering and live demos
- [Backseat Driver](https://github.com/BetterThanTomorrow/backseat-driver) extension: for Copilot + Calva REPL integration


### Start the Build Task

1. Run the **Start Dev Environment** task: (<kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>B</kbd>)

This will start three nREPL servers in integrated terminals as VS Code Tasks:

* bb nREPL server
* Scittle REPL
* Euppup REPL

### Connect the Joyride REPL

Because reasons the Joyride REPL can't be started by VS Code Tasks. But you really should start it.
* From the command palette, search: "**Start joy**".

It will find the right command for you (**Calva: Start Joyride REPL and Connect**).

You should see the the <kbd>joyride</kbd> REPL session active in the status bar. (<kbd>.md → joyride</kbd>)

### Jack-in to Babashka

Not strictly a prerequisite for this project, but it is a prerequisite for a Clojure dev to alway have the Babashka REPL in reach.

1. Command Palette: **Calva: Connect to a Running REPL Server in the Project**
   1. Select **calva-at-nubank** as Project root
   1. Select **Babashka** as Project Type/Connect Sequence

When Calva can't decide the routing comeption from two session for a given file, the first session will win. The status bar will still show (<kbd>.md → joyride</kbd>) as the active session. Click the session indicator to see that the <kbd>bb</kbd> REPL session is available.

### Keybindings

The slide navigator is activated automatically when the workspace opens (via [workspace_activate.cljs](.joyride/scripts/workspace_activate.cljs)). Use <kbd>F5</kbd> to enter Zen Mode. Press again to start the slideshow. Arrow keys and Page Up/Down navigate between slides. <kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>Cmd</kbd>+<kbd>Left</kbd> restarts from the first slide.

For this (and more) to work you need to copy these keybindings to your `keybindings.json`:

```json
//// BEGIN DEMO BINDINGS ////

// The Missing Command Palette
{
  "title": "Keybinding Command Palette",
  "category": "Joyride",
  "key": "ctrl+alt+j ctrl+alt+j",
  "command": "joyride.runCode",
  "args": "(require '[keybinding-palette :as kp] :reload) (kp/show-palette!+)"
}

// Slide Navigation
{
  "title": "Activate Slide Mode",
  "category": "Next-slide",
  "key": "ctrl+alt+j s",
  "command": "joyride.runCode",
  "args": "(prezo.next-slide/activate!)"
},
{
  "title": "Deactivate Slide Mode",
  "category": "Next-slide",
  "key": "ctrl+alt+j ctrl+alt+s",
  "command": "joyride.runCode",
  "args": "(prezo.next-slide/deactivate!)"
},
{
  "title": "Show Markdown Preview",
  "category": "Next-slide",
  "key": "ctrl+alt+j ctrl+alt+m",
  "command": "markdown.showPreview"
},
{
  "title": "Next Slide",
  "category": "Next-slide",
  "key": "right",
  "command": "joyride.runCode",
  "args": "(prezo.next-slide/next! true)",
  "when": "next-slide:active && !inputFocus"
},
{
  "title": "Previous Slide",
  "category": "Next-slide",
  "key": "left",
  "command": "joyride.runCode",
  "args": "(prezo.next-slide/next! false)",
  "when": "next-slide:active && !inputFocus"
},
{
  "title": "Next Slide",
  "category": "Next-slide",
  "key": "pagedown",
  "command": "joyride.runCode",
  "args": "(prezo.next-slide/next! true)",
  "when": "next-slide:active"
},
{
  "title": "Previous Slide",
  "category": "Next-slide",
  "key": "pageup",
  "command": "joyride.runCode",
  "args": "(prezo.next-slide/next! false)",
  "when": "next-slide:active"
},
{
  "title": "Enter Zen Mode",
  "category": "Next-slide",
  "key": "F5",
  "command": "workbench.action.toggleZenMode",
  "when": "next-slide:active && !inZenMode"
},
{
  "title": "Show Current Slide",
  "category": "Next-slide",
  "key": "F5",
  "command": "joyride.runCode",
  "args": "(prezo.next-slide/current!)",
  "when": "next-slide:active && inZenMode"
},
{
  "title": "Restart Presentation",
  "category": "Next-slide",
  "key": "ctrl+alt+cmd+left",
  "command": "joyride.runCode",
  "args": "(prezo.next-slide/restart!)"
}

// Speaker Notes
{
  "title": "Prepare Speaker Notes",
  "category": "Next-slide",
  "key": "ctrl+alt+j ctrl+n",
  "command": "joyride.runCode",
  "args": "(prezo.next-slide-notes/prepare!)"
},
{
  "title": "Edit Active Note",
  "category": "Next-slide",
  "key": "ctrl+alt+j shift+n",
  "command": "joyride.runCode",
  "args": "(prezo.next-slide-notes/edit-active-note!)"
},
{
  "title": "Print Speaker Notes",
  "category": "Next-slide",
  "key": "ctrl+alt+j alt+n",
  "command": "joyride.runCode",
  "args": "(prezo.next-slide-notes/print!)"
},
{
  "title": "Toggle Speaker Notes",
  "category": "Next-slide",
  "key": "ctrl+alt+j ctrl+alt+n",
  "command": "joyride.runCode",
  "args": "(prezo.next-slide-notes/toggle!)"
},

// Toggle line numbers (works in Zen Mode)
{
  "title": "Toggle Line Numbers",
  "category": "Editor",
  "key": "ctrl+alt+j l",
  "command": "joyride.runCode",
  "args": "(set! (.-lineNumbers vscode/window.activeTextEditor.options) ({1 0 0 1} (.-lineNumbers vscode/window.activeTextEditor.options)))"
},

// Flares and Utilities
{
  "title": "Show Flares Picker",
  "category": "Flares",
  "key": "ctrl+alt+j ctrl+shift+f",
  "command": "joyride.runCode",
  "args": "(do (require '[flares] :reload) (flares/show-flares-picker!+))"
},
{
  "title": "Open URL in Sidebar",
  "category": "Flares",
  "key": "ctrl+alt+j ctrl+alt+b",
  "command": "joyride.runCode",
  "args": "(do (require '[flares] :reload) (flares/prompt-and-open-url-in-sidebar!+))"
},
{
  "title": "Open URL as Panel",
  "category": "Flares",
  "key": "ctrl+alt+j alt+b",
  "command": "joyride.runCode",
  "args": "(do (require '[flares] :reload) (flares/prompt-and-open-url-as-panel!+))"
},

//// END DEMO BINDINGS ////
```

The first one opens the **keybinding palette**: a searchable picker that shows all your keybindings with descriptions. A.k.a. **The Missing Command Palette**. Like me, you will wonder how you ever coped without it.

## The Seven REPL Connections

When you open the Calva REPL menu, you get five custom [Calva Connect Sequences](https://calva.io/connect-sequences/) plus Calva's built-in Babashka and Joyride sequences. Three use **Jack-in** (Calva starts the REPL for you), three use **Connect** (you start the REPL first, then Calva connects to it), and Joyride has its own dedicated start command.

The custom sequences are defined in [settings](.vscode/settings.json).

| Sequence | Mode | Config | Use case | Ports |
|---|---|---|---|---|
| **Shadow fullstack** | Jack-in | Customized deps.edn + shadow-cljs (browser) | Full-stack development: backend + ClojureScript frontend | auto |
| **pirate-lang** | Jack-in | Customized deps.edn | Language experiments, no Docker needed | auto |
| **Docker REPL (pirate-lang)** | Jack-in | Customized deps.edn with custom command line | Same project, isolated in Docker | 7888 |
| **Epupp REPL** | Connect | Customized Scittle (browser [SCI](https://github.com/babashka/sci)) | Browser tampering, live demos on calva.io | 3339 / 3340 |
| **Scittle Tic-Tac-Toe REPL** | Connect | Customized Scittle | Replicant tic-tac-toe game in a Joyride Flare | 1339 / 1340 |
| **Babashka REPL** | Connect | Babashka (built-in sequence) | Workspace scripting and tasks | auto (`bb/.nrepl-port`) |
| **Joyride REPL** | Calva dedicated command | Joyride (built-in) | Make VS Code Truly Yours | auto |

The three **Connect** sequences require the background REPLs to be running first. Run the **Start Dev Environment** build task (<kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>B</kbd>) to start all three.

> [!NOTE]
> You can have multiple connections open simultaneously. Calva routes files to the right REPL automatically. (Plus you can pin a session.) With Calva Backseat Driver the AI agent has tools to list the sequences.

## The Projects

### pirate-lang: Clojure Language Playground

A super silly one-file Clojure project, with a Docker option for isolated development.

**Start it:**
* Open [pirate_lang.clj](projects/pirate-lang/src/pez/pirate_lang.clj)
* From the Command Palette: **Start your project with a REPL and connect (a.k.a. Jack-in)**
* Select the **calva-at-nubank/projects/pirate-lang** Project Root
* Select the **pirate-lang** Project Type/Connect Sequence

The repl-session <kbd>pirate-lang</kbd> should show as active in the status bar.

**What to try:**
- Load the file in the REPL:
  1. Command Palette: **Calva: Load/Evaluate Current File and its Requires/Dependencies**
- Evaluate some forms
  * **Top level form:** <kbd>alt/option</kbd>+<kbd>enter</kbd>
  * **Current form:** <kbd>ctrl</kbd>+<kbd>enter</kbd>
- Evaluate the forms in the first Rich Comment Form as **Top Level Form**

> [!NOTE]
> **Rich Comment Form (RCF)** is the `(comment ...)` form.
> See: https://calva.io/rich-comments/

There's not much more to it. It's a **toy/joke** project. 😀

### pirate-lang: Dockerized

Same project, isolated in a container. Exposes nREPL on port 7888. Source, test, and resource directories are mounted as volumes so edits are live.

The connect sequence uses a [custom Jack-in command line](https://calva.io/connect-sequences/#custom-command-line) (`bb docker-repl`) that builds and runs the container automatically. Requires Docker.

**Start it:**
* Open [pirate_lang.clj](projects/pirate-lang/src/pez/pirate_lang.clj)
* Jack-in to the REPL, same as above, but select the **Docker REPL (pirate-lang)** Project Type/Connect Sequence

> [!NOTE]
> The REPL session is named <kbd>pirate-lang-docker</kbd>, if the status bar still says <kbd>pairate-lang</kbd> it is because you still have that repl connected. Since it is targeting the same files, the auto-route doesn't know which one, but you can manually select the session to target by clicking the session button in the status bar and pinning a session.

### shadow-w-backend: Mini Fullstack App

A tiny but complete full-stack app following Thomas Heller's [Fullstack Workflow with shadow-cljs](https://code.thheller.com/blog/shadow-cljs/2024/10/18/fullstack-cljs-workflow-with-shadow-cljs.html). Clojure backend + ClojureScript frontend, hot-reloading on both sides.

**Start it:**
* Open [repl.clj](projects/shadow-w-backend/src/dev/repl.clj)
1. Jack-in: (<kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>C</kbd> <kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>J</kbd>), then select **Shadow fullstack**
2. Load the file, and evaluate `(go!)` in the Rich Comment to start the backend
3. The frontend should be started at [localhost:3000](http://localhost:3000). The most fun way to do it is to click the **Flares** button in the status bar -> **Open URL in Sidebar...** -> `http://localhost:3000`. The Flare manager script is in [.joyride/src/flares.cljs](.joyride/src/flares.cljs).

**What to try:**
- Evaluate stuff in [server.clj](projects/shadow-w-backend/src/main/acme/server.clj) (backend) or [app.cljs](projects/shadow-w-backend/src/main/acme/frontend/app.cljs) (e.g.)

As you navigate between `server.clj` and `app.cljs` you should see the REPL sessions <kbd>backend</kbd> and <kbd>frontend</kbd>, respectively, as connected in the status bar. (Unless you have pinned some session as per above. If so unpin!)

## Joyride

Finally!

### Scittle Tic-Tac-Toe: Replicantin + the Scittle REPL in a Flare

A tic-tac-toe game built with Scittle and [Replicant](https://github.com/cjohansen/replicant). Runs in a Joyride Flare (WebView panel).

**Start it:**
1. Open [ttt_flare.cljs](.joyride/src/scittle-replicant-tic-tac-toe/ttt_flare.cljs)
1. Load the file in the REPL
1. Evaluate `(replicant-ttt)` in the bottom Rich Comment.
1. The game runs in a Joyride sidebar Flare (Flare = Major WebView panel convenience)

When you've tired of playing the game you will want to play it using the REPL. Luckily the Flare is running with a Scittle REPL ready and you have a SCI browser REPL server running (the **Scittle REPL** task):

1. Connect the **Scittle Tic-Tac-Toe** REPL. Connect running repl, etcetera...
1. Open [replicant_tictactoe/core.cljs](.joyride/src/scittle-replicant-tic-tac-toe/resources/scittle/replicant_tictactoe/core.cljs)
   * You should see the REPL session <kbd>tic-tac-toe</kbd> become active
1. Evaluate the forms in the Rich Comment
1. Select the RCF and ask Copilot: *Wanna play some tic tac toe in the repl? You start.*

### Live REPL Examples

Explore [live_examples.cljs](.joyride/src/live_examples.cljs): assorted Joyride patterns (status bar items, information messages, VS Code API demos). It is designed so that you can evaluate one form at a time. E.g. You can use ParEdit to move forward one form at a time and evaluate top level form at each. With the cursor immediately after `:start/here`:

0. Observe and ponder
1. <kbd>Ctrl</kbd>+<kbd>right</kbd> (Win/Linux) / <kbd>Alt</kbd>+<kbd>right</kbd> (Mac)
2. <kbd>Alt</kbd>+<kbd>Enter</kbd>
3. Repeat from **0**

**Tip**: This project is configured to hide line numbers. In [live_examples.cljs](.joyride/src/live_examples.cljs) there's a form for toggling them on and off. There's also a keybinding (<kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>J</kbd> <kbd>L</kbd>) and a custom REPL command (`l`) configured for it. The keybinding uses `joyride.runCode` so it works without an nREPL connection, while the custom REPL command requires the <kbd>joyride</kbd> REPL session to be connected.

> [!NOTE]
> VS Code has built in command for toggling line numbers, you say? Fair point, but have you tried it with **Zen Mode** on? Check mate.

### Next Slide, Please

This entire project is also a Joyride showcase. The presentation system, the demo tooling, and several general-purpose scripts are all Joyride code. And it is all meant for your global/user Joyride config (see about [globalizing](#workspaceglobal-sync-bb-localize--bb-globalize) below).

#### Presentation Infrastructure

- [next_slide.cljs](.joyride/src/prezo/next_slide.cljs): slide navigator with keyboard shortcuts (arrow keys, page up/down)
- [next_slide_notes.cljs](.joyride/src/prezo/next_slide_notes.cljs): speaker notes companion
  * Not mentioned as a prerequisite, but if you have Pandoc installed you can convert the notes to a PDF document
  * Check the keybindings (and the [Keybindings Palette](#the-missing-command-palette)) for notes related things
- [showtime.cljs](.joyride/src/showtime.cljs): status bar timer for tracking presentation time (click to start -> click to stop -> click to restart)

#### Flares (WebView Panels)

- [flares.cljs](.joyride/src/flares.cljs): URL picker and sidebar slot manager with history
- [flares_examples.cljs](.joyride/src/flares_examples.cljs): live REPL demos: Fibonacci sequences, animated SVG orbits, sidebar panels, Hiccup and HTML rendering
- [assets/example-flare.edn](assets/example-flare.edn): an EDN Flare with embedded Scittle

#### The Missing Command Palette

[keybinding_palette.cljs](.joyride/scripts/keybinding_palette.cljs): a command palette for your keybindings (reads `keybindings.json`, shows a searchable picker, uses an npm module for parsing JSONC).

#### Paste as Markdown

[pastedown.cljs](.joyride/src/pastedown.cljs): adds a "Paste as Markdown" option to VS Code's paste menu. Converts rich HTML (tables, links, formatted text) to clean Markdown, and rewrites Copilot Chat file links to workspace-relative paths. See [PASTEDOWN.md](PASTEDOWN.md) for activation and keybinding setup.

### Extending with Copilot

There are [Joyride-specific skills](.github/skills/joyride/SKILL.md) for general Joyride development.

There is also an example Copilot prompt for extending the slide system at [.github/prompts/Implement.prompt.md](.github/prompts/Implement.prompt.md). **Try it, please!**

## Epupp: Browser Tampering

[Epupp](https://github.com/PEZ/epupp) connects a ClojureScript REPL to web pages in your browser, letting you modify them live.

### Live Demo Helpers

[live-tampers/calva_io.cljs](live-tampers/calva_io.cljs) has functions for demoing Epupp on [calva.io](https://calva.io). For this you will need Epupp and the SCI Browser REPL server serving the Epupp defaults ports. (You have it running, it is the **Epupp REPL** task).

0. Install Epupp (see [Prerequisites](#prerequisites) for links)
1. Open [calva.io](https://calva.io) in your browser (the Flare you opened earlier won't do)
1. Open the Epupp popup (click the Epupp icon in the Browser toolbar)
1. Click **Connect**
1. Ensure that **Reconnect connected tabs on navigation** is enabled
1. Open [live-tampers/calva_io.cljs](live-tampers/calva_io.cljs)
   * You should see the <kbd>epupp</kbd> REPL session activate
1. Load the file
1. Play with the Rich Comment Form at the bottom of the file:

It is not just for jokes. If you ask Copilot about Calva and tell it to use the **Epupp** REPL for up-to-date info. You will see that some of the functions are quite helpful for the bot.

- `(search! "jack-in")`: opens the MkDocs search overlay
- `(go! 0)`: navigates to a search result
- `(navigate! "https://calva.io/")`: navigates to any URL
- `(toggle-dark-mode!)`: toggles a CSS dark mode injection (the joke part)

> [!NOTE]
> The Calva site is not an SPA, so the REPL will fully unload when navigating. This can potentially trip the AI up, because it will hang on the REPL response which will never come. This project has a Skill teaching Copilot how to avoid the hang, and what to expect, but it hinges on that it uses the Skill...

### Userscripts

Taking the **calva.ip dark mode** joke further. You can install it as an auto-running userscript. What's even better: You can do it from the REPL.

0. With the Epupp REPL still connected in the Browser and in VS Code
1. In the Epupp popup, enable **Allow REPL FS Sync for this tab**
1. Open [epupp-userscripts/pez/calva_io_darkmode.cljs](epupp-userscripts/pez/calva_io_darkmode.cljs)
1. From the Calva Custom REPL Commands Palette (<kbd>ctrl+alt/option<+space</kbd> <kbd>ctrl+alt/option<+space</kbd>): Select **Upload current Epupp userscript**
   * In the Epupp popup you should now see the script in the **Auto-run for this page** section
1. Reload the page

The Epupp popup UI has a button for deleting the script. Epupp also has a Development Tools Panel for inspecting, authoring, and live testing Epupp code and userscripts. If you hold off with deleting the script you can:

1. Open the browser dev tools
1. Click the **Epupp** tab
1. From the Epupp popup UI, click the **inspect** button instead of **delete**.

The panel editor also lets you install scripts. And there are also more ways to install scripts, to much for this already long README...

### Learn about Epupp

* https://github.com/PEZ/epupp: For info about Epupp and how to use
* https://youtu.be/CuEWN5yYVa8: For a demo of Copilot creating a really useful userscript
* https://github.com/PEZ/my-epupp-hq for a template home for your Epupp adventures and for managing your userscripts (it uses `bb` tasks for syncing to and from Epupp)

## Automation Recipes

### Global Babashka Tasks (bbg)

**bbg** is a pattern for running Babashka tasks from any directory. Put a `bb.edn` + scripts in `~/.config/bbg/`, symlink a wrapper to `~/bin/bbg`, and you get project-independent automation with shell completions.

The wrapper script:

```bash
#!/usr/bin/env bash
bb --config ~/.config/bbg/bb.edn "$@"
```

The [slides/bbg.md](slides/bbg.md) slide covers the full recipe. For a working example, you can copy as much and as little you want from, see my personal bbg tasks: [github.com/PEZ/my-bbg](https://github.com/PEZ/my-bbg). It has several nifty tasks (including an `mdq` Markdown query tool).

### Workspace <-> Global Sync (bb localize / bb globalize)

This workspace uses `bb localize` and `bb globalize` to keep Joyride scripts and Copilot customization files in sync between the workspace and your global user configuration.

```bash
bb localize    # Copy from global ~/.config/joyride/ and ~/.copilot/ into this workspace
bb globalize   # Copy from this workspace back to global
```

What gets synced is defined in [dependencies-sync.edn](dependencies-sync.edn): Joyride source files, npm deps, Copilot skills, agents, and prompts. The logic lives in [scripts/dependencies_sync.clj](scripts/dependencies_sync.clj).

The idea: develop and refine your automation in one workspace, then `globalize` to make it your default. Next project, `localize` pulls it in. You could imagine extending this to **bbg** tasks too.

The Joyride scripts and Copilot files that `globalize` syncs are general-purpose tools that ideally live at the user level (`~/.config/joyride/` and `~/.copilot/`). Before running `bb globalize`, review [dependencies-sync.edn](dependencies-sync.edn) to see what will be copied and remove any entries you don't want in your global config. After globalizing, you'll also want to:

1. Add the relevant scripts to your Joyride user activation (`~/.config/joyride/scripts/user_activate.cljs`) if they need activation (only **Pastedown** does, see [PASTEDOWN.md](PASTEDOWN.md))
2. Run `npm install` in `~/.config/joyride/` to pick up any new npm dependencies
3. Copy any keybindings you want from the [Keybindings](#keybindings) section to your `keybindings.json`
4. Remove the globalized scripts and files from this project to avoid conflicts and confusion

## Copilot & AI

The workspace includes Copilot customization as working examples:

- [AGENTS.md](AGENTS.md): workspace-level agent operating principles
- [.github/agents/](.github/agents/): custom agent modes (Clojure editor, interactive programming, reviewer)
- [.github/skills/](.github/skills/): domain skills (Babashka, Joyride, Epupp, next-slide-editor, and more)
- [.github/prompts/](.github/prompts/): reusable prompts

[Backseat Driver](https://github.com/BetterThanTomorrow/backseat-driver) integrates Copilot with Calva's REPL: the AI can evaluate code, do structural editing, and look up symbols. And it is very easy to use. Install the extension and it works. (It is also quite simple. We're not trading simple for easy here!)

### Resources

- [CalvaTV](https://www.youtube.com/@CalvaTV): recorded demos and tutorials
- [calva.io](https://calva.io): Calva documentation
- [Joyride](https://github.com/BetterThanTomorrow/joyride): VS Code scripting in ClojureScript
- [Backseat Driver](https://github.com/BetterThanTomorrow/backseat-driver): Gives Copilot Calva tools, including the REPL
- [PEZ/my-bbg](https://github.com/PEZ/my-bbg): My personal `bbg` global tasks repo
- [Replicant](https://github.com/cjohansen/replicant): pure ClojureScript VDOM rendering

### Happy coding! ❤️
