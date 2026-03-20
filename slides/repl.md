<div class="slide cols-2">

# `(= REPL nREPL) => false`

<div class="col">

## REPL

**R**ead **E**val **P**rint **L**oop: a process built into the Clojure runtime.

* Started together with the application (starts the application, even)
* Compiles and recompiles, whole or parts, of the application.

Provides the interactive programming environment that is Clojure's **Super Power #1**, imo.

<div class="icon-gallery sm-img mt-2">

![Clojure](../images/clojure-logo.png)
![ClojureScript](../images/clojurescript-logo.png)

<br>

![SCI](../images/sci-icon.png)
![Babashka](../images/babashka-logo.svg)
![nbb](../images/nbb-logo.svg)
![Joyride](../images/joyride-icon.png)
![Jank](../images/jank-logo.png)

</div>

</div>
<div class="col">

## nREPL

**n**etwork **REPL** - a protocol and clients/servers.

1. A _protocol_ for connecting tooling to the Clojure REPL
2. Two programs:
   * The _server_: running in process with the Clojure REPL
   * The _client_: running in your editor, AI agent harness, CLI command tool, etcetera.

Reference [nREPL project](https://nrepl.org)

Ops: `eval`, `load-file`, ...

Custom ops via **middleware**, `cider-nrepl`

<div class="icon-gallery mt-2">

![nREPL](../images/nrepl-icon.svg)

</div>

</div>
</div>
