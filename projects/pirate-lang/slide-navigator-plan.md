# Synthesized Plan: Slide Navigator Sidebar

**Produce a Joyride workspace script** that creates a sidebar view listing presentation slides using Flares, Replicant, and the uniflow pattern. Two files:

## File 1: `.joyride/src/prezo/slide_navigator.cljs`
**Host module** — lifecycle, state bridging, message handling.

**Namespace:** `prezo.slide-navigator`
**Requires:** `prezo.next-slide`, `joyride.flare`, `joyride.core`, `promesa.core`, `"vscode"`, `clojure.string`

**`init!` function** (returns disposable via `#js {:dispose (fn [] ...)}`):
1. Read slide list via `(next-slide/slides-list+)`
2. Enrich: for each path, derive `:label` (filename sans `slides/` prefix & `.md`, hyphens→spaces, capitalize), check `:notes?` via `vscode/workspace.fs.stat` on `<name>-notes.md` (catch → false)
3. Create flare with `flare/flare!+`:
   - `:key :sidebar-5`, `:title "Slides"`, `:reveal? false`, `:preserve-focus? true`
   - `:webview-options {:enableScripts true :retainContextWhenHidden true}`
   - `:html` — hiccup with `<head>` containing: Scittle CDN `0.7.30` (scittle.js, scittle.nrepl.js at port 1340, scittle.replicant.js), inline `<style>` with VS Code CSS variables, `<script type="application/x-scittle" src="...">` pointing to the Scittle file
   - `:message-handler` — dispatches on `(.-type msg)`:
     - `"ready"` → send full state via `post-message!+` with `{:type "init" :data {:slides [...enriched maps...] :active-index N :count N}}`
     - `"navigate"` → `(next-slide/show-slide-by-name!+ path)` (this updates `!state`, which triggers watcher)
     - `"edit"` → `vscode/workspace.openTextDocument` + `vscode/window.showTextDocument` with the slide URI
     - `"edit-notes"` → same for the `-notes.md` path
4. Watch `prezo.next-slide/!state`: on `:next/active-slide` change, send `{:type "active" :data {:active-index N}}` to webview
5. Register `vscode/window.onDidChangeActiveTextEditor`: extract relative path, if it matches a slide, send `{:type "active" :data {:active-index (matching index)}}` — **read-only**, don't mutate `!state`
6. Return `#js {:dispose (fn [] ...)}` that: `remove-watch` on `!state`, `.dispose` editor listener, `flare/close! :sidebar-5`

**Integration:** Add `prezo.slide-navigator` to `workspace_activate.cljs` requires, call `(push-disposable (slide-navigator/init!))` in `my-main`.

**Inline CSS** (in `<style>` block, using VS Code CSS variables):
- Body: `--vscode-sideBar-background`, `--vscode-font-family`
- `.header`: flex between title & count badge (`--vscode-badge-*`), border-bottom
- `.slide-row`: flex, `padding: 4px 12px`, `cursor: pointer`, `border-left: 3px solid transparent`
- `.slide-row:hover`: `--vscode-list-hoverBackground`
- `.slide-row.active`: `--vscode-list-activeSelectionBackground/Foreground`, `border-left-color: --vscode-focusBorder`
- `.slide-number`: `min-width: 24px`, right-aligned, `opacity: 0.6`
- `.slide-label`: `flex: 1`, `text-overflow: ellipsis`
- `.actions`: `opacity: 0`, `.slide-row:hover .actions { opacity: 1 }`, transition
- `.action-btn`: no bg/border, `--vscode-icon-foreground`, hover: `--vscode-textLink-foreground`

## File 2: `.joyride/src/prezo/slide_navigator_ui.cljs`
**Scittle webview app** — Replicant rendering, loaded via `<script type="application/x-scittle">`.

**Namespace:** `prezo.slide-navigator-ui`
**Requires:** `replicant.dom :as r`

1. `(def vscode (js/acquireVsCodeApi))` — once only
2. `(defonce !store (atom {:slides [] :active-index 0 :count 0}))`
3. `r/set-dispatch!` handler — receives `(event-data action)`, dispatches `[:navigate path]`, `[:edit path]`, `[:edit-notes path]` → each calls `(.postMessage vscode (clj->js {:type ... :data {:path ...}}))`. For `:edit` and `:edit-notes`, call `(.stopPropagation (:replicant/js-event event-data))` to prevent the row's `:navigate` click from also firing.
4. `add-watch !store` → `r/render` on `#app` with `(slide-navigator @!store)`
5. `window "message"` listener:
   - `"init"` → `reset! !store` with full data (slides, active-index, count)
   - `"active"` → `swap! !store assoc :active-index (:active-index data)`
6. Post `{:type "ready"}` to host at the end of init

**Render functions** (pure hiccup, Replicant event dispatch vectors):

```clojure
(defn slide-row [{:keys [path label notes? index]} active-index]
  [:div.slide-row {:class (when (= index active-index) "active")
                   :on {:click [:navigate path]}}
   [:span.slide-number (inc index)]
   [:span.slide-label label]
   [:div.actions
    [:button.action-btn {:on {:click [:edit path]} :title "Edit slide"} "✎"]
    (when notes?
      [:button.action-btn {:on {:click [:edit-notes (notes-path path)] :title "Edit notes"}} "📋"])]])

(defn slide-navigator [{:keys [slides active-index count]}]
  [:div.navigator
   [:div.header
    [:span "Slides"]
    [:span.count-badge (str count)]]
   [:div.slide-list
    (map-indexed (fn [i slide] (slide-row (assoc slide :index i) active-index)) slides)]])
```

## Message Protocol Summary

| Direction | Type | Payload | Trigger |
|---|---|---|---|
| Webview→Host | `"ready"` | `{}` | Scittle initialized |
| Host→Webview | `"init"` | `{:slides [{:path :label :notes?}...] :active-index N :count N}` | On `"ready"` |
| Host→Webview | `"active"` | `{:active-index N}` | `!state` watch or editor change |
| Webview→Host | `"navigate"` | `{:path "slides/foo.md"}` | Click slide row |
| Webview→Host | `"edit"` | `{:path "slides/foo.md"}` | Click edit button |
| Webview→Host | `"edit-notes"` | `{:path "slides/foo-notes.md"}` | Click notes button |

## Rich comment form
Include a `comment` block with: `(init!)`, `(swap! next-slide/!state assoc :next/active-slide 5)`, `(flare/post-message!+ :sidebar-5 ...)`, `(flare/ls)`.
