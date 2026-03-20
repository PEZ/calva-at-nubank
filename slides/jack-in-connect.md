<div class="slide cols-2">

# Jack-in and Connect

<div class="col">

## Jack-in

Starts the REPL, and then **Connects** it

* Constructs the command line
* Injects dependencies (nREPL, cider-nrepl, custom middleware)
* Injects options (aliases, main)
* Uses Project Types/Connect Sequences
* Supports fully custom command lines
  * Environment and/or substitution variables

[hello.clj](../etc/hello.clj), [pirate_lang.clj](../projects/pirate-lang/src/pez/pirate_lang.clj)
</div>

<div class="col">

## Connect

Connects to a running REPL

* REPL in **external terminal/process**: survives VS Code restart
* REPL in **integrated terminal**: survives window reload
* Tip: **Copy Jack-in command line** → start manually
* Tip: [.vscode/tasks.json](../.vscode/tasks.json)
* Generic project type for unknown REPL types
* Uses Project Types/Connect Sequences

</div>

***Jack-in** = **Connect** + dependency injection + process management*

***Connection**: nREPL client <-> server*

***Session**: Code in the editor <-> Clojure REPL. 1-2 sessions per connection.*

<br><br><br><br><br>

```json
    // Local REPL
    {
      "name": "pirate-lang",
      "projectType": "deps.edn",
      "projectRootPath": [
        "projects",
        "pirate-lang"
      ],
      "cljsType": "none",
      "replSessionNames": {
        // "primary": "pirate-lang"
      },
      "menuSelections": {
        "cljAliases": [
          "allow-attach-self",
          "dev",
          "test"
        ]
      }
    },

    // REPL in a container
    {
      "name": "Docker REPL (pirate-lang)",
      "projectType": "deps.edn",
      "projectRootPath": [
        "projects",
        "pirate-lang"
      ],
      "customJackInCommandLine": "bb docker-repl",
      "cljsType": "none",
      "menuSelections": {
        "cljAliases": [
          "allow-attach-self",
          "dev",
          "test"
        ]
      }
    },
```
</div>

