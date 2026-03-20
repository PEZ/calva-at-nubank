<div class="slide cols-4-8">

# bbg — Global/user Babashka tasks

<div class="col">

**bb tasks available from any directory**

```sh
$ bbg tasks #(e.g.)
loc    Count lines of code
clj    Manage clojure installs
config Manage config repos
java   Manage Java versions
```

```
~/.config/bbg/
├─ bbg
├─ bb.edn
├─ scripts/*.clj
└─ completions.zsh
```

`~/bin/bbg` → `~/.config/bbg/`

```bash
#!/usr/bin/env bash
CALLER_CWD="$(pwd)"
cd ~/.config/bbg
bb "$@" --cwd "$CALLER_CWD"
```

</div>
<div class="col">

`source ~/.config/bbg/completions.zsh` *Handles options like --status, --update, ...*
```sh
_bbg() {
    if (( CURRENT == 2 )); then
        local tasks=(`bbg tasks | tail -n +3 | cut -f1 -d ' '`)
        compadd -a tasks
    else
        local task="${words[2]}"
        local opts=(`bbg tasks | grep "^${task} " | grep -oE '\-\-[a-z-]+'`)
        if (( ${#opts} )); then
            compadd -a opts
        fi
    fi
}
compdef _bbg bbg
```




`~/.config/bbg/bb.edn`

```clojure
  :enter (def cwd (:cwd (cli/parse-opts *command-line-args*)))

  loc {:doc "Count lines of code (excludes ...)"
       :task (loc/count! {:cwd cwd})}
```
</div>

</div>
