# Pastedown: Paste as Markdown

Pastedown adds a **Paste as Markdown** option to VS Code's paste menu. Copy rich content (HTML tables, links, formatted text) and paste it as clean Markdown instead of plain text. It also converts Copilot Chat's internal file links to workspace-relative Markdown links with line numbers.

The source is [.joyride/src/pastedown.cljs](.joyride/src/pastedown.cljs).

## Activation

Pastedown registers itself as a VS Code document paste provider. It needs to be activated when VS activates things. This is done with your Joyride user activation script (`~/.config/joyride/scripts/user_activate.cljs`):

```clojure
(ns user-activate
  (:require pastedown
            ...))

(defn- my-main []
  (pastedown/activate!)
  ...)
```

You can also activate it manually from a Joyride REPL:

```clojure
(require 'pastedown :reload)
(pastedown/activate!)
```

## Usage

Once activated, when you paste (<kbd>Cmd</kbd>+<kbd>V</kbd> / <kbd>Ctrl</kbd>+<kbd>V</kbd>) in any editor, VS Code shows a small paste selector widget at the end of the pasted text. Click it (or press <kbd>Cmd</kbd>+<kbd>.</kbd> / <kbd>Ctrl</kbd>+<kbd>.</kbd>) to choose **Paste as Markdown**. This behavior is controlled by the `editor.pasteAs.showPasteSelector` setting.

### Keybindings

For direct keybinding access (bypassing the paste selector), add these to your `keybindings.json`:

```json
// Paste as Markdown in Copilot Chat
{
  "key": "ctrl+alt+j ctrl+alt+v",
  "command": "joyride.runCode",
  "when": "inChatInput",
  "args": "(require 'pastedown :reload) (pastedown/pastedown-in-chat!)"
},
// Paste as Markdown in editors
{
  "key": "ctrl+alt+j ctrl+alt+v",
  "command": "editor.action.pasteAs",
  "when": "editorTextFocus",
  "args": {
    "kind": "pastedown"
  }
}
```

## Dependencies

Pastedown uses [turndown](https://github.com/mixmark-io/turndown) for HTML-to-Markdown conversion with the GFM (GitHub Flavored Markdown) plugin. These npm packages need to be installed in the Joyride directory:

```bash
cd .joyride && npm install
```

(They're listed in [.joyride/package.json](.joyride/package.json).)
