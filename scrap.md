Scripts, harness, config > instructions
- Instead of updating AI instructions, create/update bb task


I think mainly it is that we don't need, and never have needed, the auto-generated agents.md files as much as we have thought. By definition that will be things the agent can discover itself.

It's a trade-off. You also don't want each agent session to start with discovery of the same important things over and over.

I think a valid approach is to never auto-generate the file. Doesn't mean you shouldn't keep one, nor that you shouldn't use the agent to tend it. But you should be super picky with what goes in there. Common mistakes are candidates. But these are also often signals that something should be fixed. Not having the pit-fall is better than teaching the agent to avoid it. 😀

As the models get smarter it also gets to be more and more viable to give it many different documents to read, choosing wisely depending on the task at hand.

e2e tests

REPL and nREPL crash course

Custom REPL Commands substitution variables:
- `$line`, `$column` - cursor position
- `$file`, `$file-text` - file path / full text
- `$ns`, `$editor-ns`, `$ns-form` - namespace context
- `$selection` - selected text
- `$current-form`, `$current-pair`, `$enclosing-form`, `$top-level-form` - structural forms
- `$current-fn`, `$top-level-defined-symbol` - symbol helpers
- `$head`, `$tail` - text before/after cursor in current list
- `$hover-text`, `$hover-top-level-defined-symbol` - hover snippet variants
- Modifiers: `${variable|modifier|args}`, chainable with `|`
  - `pr-str` - stringify
  - `replace` - regex replace (pattern, replacement)
  - `replace-first` - first match only
  - ClojureScript semantics, escape `|` `{` `}` with `\`

Calva extension model
- runCommands
- API
- Calva PowerTools
- Joyride

--headless vs --headed

Configs
- File excludes
- Linters, keep the lint report clean! A trust issue.
- bb tasks
- VS Code tasks/watchers
- Ignores
- Keyboard shortcuts

Workspaces
- Add important deps as folders
- Place your AI instructions in a separate folder
- Let your AI produce docs in a separate folder
- Use a separate folder for your dev config
- Keeps your workspace storage accross project folder renames

AI
- Agent first
- VS Code Native
- Copilot Native

- Prompting
  - Custom agents
  - Parallel subagents, parallize everything
  - askQuestions
  - While instructions, tooling, prior art, etc is building, go heavy with many agents analysing, planning, reviewing, doing. Then relax it gradually.

AI forces you to fix configuration glitches
- linting
- docker not always starting
- too much output from this or that tool
- flaky e2e tests
- always start watchers
- always start and connect your repls


AI config:
- Never let a mistake go to waste
- Teach the agent to learn
  - Watch out from when it learns the wrong things!
- Use subagents
- Use parallel subagents


Keyboard shortcuts:
```json
  {
    "command": "paredit.insertSemiColon",
    "key": ";",
    "when": "calva:keybindingsEnabled && editorLangId == clojure && editorTextFocus && paredit:keyMap == strict && !editorReadOnly && !editorHasMultipleSelections && !calva:cursorInComment"
  },
```

RunCommands:

```json
{
  "key": "shift+ctrl+alt+n",
  //"when": "editorTextFocus && editorHasSelection",
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
}
```