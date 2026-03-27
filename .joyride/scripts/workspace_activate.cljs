(ns workspace-activate
  (:require [joyride.core :as joyride]
            ["vscode" :as vscode]
            [prezo.next-slide :as next-slide]
            prezo.next-slide-notes
            showtime
            flares
            pastedown
            :reload-all))

(defonce !db (atom {:disposables []}))

;; To make the activation script re-runnable we dispose of
;; event handlers and such that we might have registered
;; in previous runs.
(defn- clear-disposables! []
  (run! (fn [disposable]
          (.dispose disposable))
        (:disposables @!db))
  (swap! !db assoc :disposables []))

;; Pushing the disposables on the extension context's
;; subscriptions will make VS Code dispose of them when the
;; Joyride extension is deactivated.
(defn- push-disposable [disposable]
  (swap! !db update :disposables conj disposable)
  (-> (joyride/extension-context)
      .-subscriptions
      (.push disposable)))

(defn- open-readme-preview! []
  (when-let [root-uri (some-> vscode/workspace .-workspaceFolders first .-uri)]
    (let [readme-uri (vscode/Uri.joinPath root-uri "README.md")
          all-tabs (mapcat (fn [group] (seq (.-tabs group)))
                           (-> vscode/window .-tabGroups .-all))
          preview-open? (some (fn [tab]
                                (and (.-viewType (.-input tab))
                                     (= (.-label tab) "Preview README.md")))
                              all-tabs)]
      (when-not preview-open?
        (vscode/commands.executeCommand "markdown.showPreview" readme-uri)))))

(defn- my-main []
  (println "Hello World, from my-main workspace_activate.cljs script")
  (clear-disposables!)
  (push-disposable
   (showtime/init!))
  (push-disposable
   (flares/init-dashboard-button!))
  (pastedown/activate!)
  (next-slide/activate!)
  (open-readme-preview!))

(when (= (joyride/invoked-script) joyride/*file*)
  (my-main))
