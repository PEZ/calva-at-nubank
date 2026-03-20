<div class="slide cols-2">

# The ClojureScript REPL

<div class="col">

## Creating the Session

Calva:

1. Connects to the server (JVM)
1. Creates a **Primary** session
   - This is used for Calva's Clojure REPL session
1. Clones the Clojure session
1. Promotes the cloned, **Secondary**, session to a ClojureScript session
   - By calling functions on Piggyback or shadow-cljs, wrapping the ClojureScript REPL environment
1. Starts the build(s)
1. Connects a build
</div>

<div class="col">

## Evaluating Code

A *ClojureScript* REPL evaluation travels through more “stations” than Clojure:

1. Code in the editor
2. nREPL server
3. **nREPL middleware** (*) intercepts eval
4. The **ClojureScript compiler** compiles to JavaScript
5. Compiled JS is sent to a **JavaScript runtime** (browser, Node.js)
6. Results travel back through the same chain

(*) [Piggieback](https://github.com/nrepl/piggieback) for Figwheel and built-in ClojureScript, shadow-cljs provides its own.

</div>

***Build**: Compiled app, 1+ builds per session.*

***Runtime**: Running app (tab, device), 0+ connections/build. 1 runtime connected at a time.*

[shadow-w-backend repl.clj](../projects/shadow-w-backend/src/dev/repl.clj)
</div>
