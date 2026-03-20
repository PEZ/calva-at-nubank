(ns live-examples
  (:require ["vscode" :as vscode]
            [joyride.core :as joy]
            [promesa.core :as p]))

;; Tip: Try ask Copilot to do something fun with Joyride
;;
;; More Joyride examples at:
;; https://github.com/BetterThanTomorrow/joyride/blob/master/examples/README.md

(comment

  ;; Show an information message and handle the button clicked
  (p/let [button (vscode/window.showInformationMessage "Hello from Joyride!"
                                                       "OK"
                                                       "Cancel")]
    (vscode/window.showInformationMessage (if button
                                            (str "You clicked: " button)
                                            "You dismissed the message")))

  ;; A way to toggle line numbers
  (set! (.-lineNumbers vscode/window.activeTextEditor.options)
        ({1 0 0 1} (.-lineNumbers vscode/window.activeTextEditor.options)))

  ;; Create a statusbar item, keeping a reference
  (def item (vscode/window.createStatusBarItem
             vscode/StatusBarAlignment.Right
             1000))

  (set! (.-text item) "0.1 + 0.2")

  (.show item)
  (.hide item)

  ;; Paint the item
  (def gold "#FFD700")

  (set! (.-color item) gold)

  ;; Make it a button
  (set! (.-command item)
        (clj->js
         {:command "joyride.runCode"
          :arguments [(str
                       '(:require '["vscode" :as vscode]
                                  '[joyride.core :as joy])
                       '(.appendLine (joy/output-channel)
                                     "Opening education")
                       '(vscode/commands.executeCommand
                         "simpleBrowser.show"
                         (str "https://" (+ 0.1 0.2) ".com")))]}))
  ;; Would be simpler to use the command `simpleBrowser.show` here

  (.show item)

  ;; We need a tooltip!
  (set! (.-tooltip item) "Educate yourself")

  ;; Update the color alpha

  (defn color-with-alpha [color alpha]
    (str color (-> alpha
                   int
                   js/Number.
                   (.toString 16)
                   (.padStart 2 "0"))))

  (color-with-alpha gold 127)
  (map (partial color-with-alpha gold) [0 127 255])
  (set! (.-color item) (color-with-alpha gold 127))

  (defn wave-alpha [alpha]
    (let [unit-alpha (/ alpha 255)
          cos-alpha (js/Math.cos (* js/Math.PI unit-alpha))
          shifted (/ (+ 1 cos-alpha) 2)]
      (* 255 shifted)))

  (map wave-alpha [0  32  64  96 128 160 192 224 256 288 320])
             ;~ [255 245 217 176 127  78  36   9   0  10  38]

  (def !alpha (atom 0))
  @!alpha
  (reset! !alpha 127)

  ;; Bring <blink> back!

  (defn nudge-color! []
    (let [color gold
          alpha (wave-alpha @!alpha)]
      (swap! !alpha (partial + 15))
      (color-with-alpha color alpha)))

  (nudge-color!)

  (defn nudge-item! []
    (set! (.-color item) (nudge-color!)))

  (nudge-item!)

  (defonce !interval-ids (atom []))
  (swap! !interval-ids conj (js/setInterval nudge-item! 16))

  (js/clearInterval (peek @!interval-ids))
  @!interval-ids
  (reset! !interval-ids [])

  (.dispose item)

  ;; Working with Extensio1n APIs

  (def calva-ext (vscode/extensions.getExtension "betterthantomorrow.calva"))
  (.-isActive calva-ext)
  (def calva (some-> calva-ext .-exports .-v1))

  (vscode/commands.executeCommand
   "simpleBrowser.show"
   "https://calva.io/api/#editorreplace")

  (-> (p/let [[top-level-form-range _] (calva.ranges.currentTopLevelForm)
              _ (calva.editor.replace
                 vscode/window.activeTextEditor
                 top-level-form-range
                 "Some new text")])
      (p/catch (fn [e]
                 (println "Error replacing text:" e))))

  ;; HTML->Hiccup
  (require '["posthtml-parser" :as parser]
           '[clojure.walk :as walk])

  (defn html->hiccup
    [html]
    (-> html
        (parser/parser)
        (js->clj :keywordize-keys true)
        (->> (into [:div])
             (walk/postwalk
              (fn [{:keys [tag attrs content] :as element}]
                (if tag
                  (into [(keyword tag) (or attrs {})] content)
                  element))))))

  (comment
    (def html "<label for=\"hw\">Foo</label><ul id=\"foo\"><li>Hello</li></ul>")
    (html->hiccup html)
    :rcf)

  ;; https://github.com/BetterThanTomorrow/joyride/blob/master/doc/api.md
  ;; More examples at: https://github.com/BetterThanTomorrow/joyride/blob/master/examples/README.md

  (require 'prezo.next-slide)

  (prezo.next-slide/current!)

  :rcf)














































































































































































































































































































































































































































































































































































































































































































































































































































































































;; ⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣀⣠⣤⣤⣴⣦⣤⣤⣄⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
;; ⠀⠀⠀⠀⠀⠀⢀⣤⣾⣿⣿⣿⣿⠿⠿⠿⠿⣿⣿⣿⣿⣶⣤⡀⠀⠀⠀⠀⠀⠀
;; ⠀⠀⠀⠀⣠⣾⣿⣿⡿⠛⠉⠀⠀⠀⠀⠀⠀⠀⠀⠉⠛⢿⣿⣿⣶⡀⠀⠀⠀⠀
;; ⠀⠀⠀⣴⣿⣿⠟⠁⠀⠀⠀⣶⣶⣶⣶⡆⠀⠀⠀⠀⠀⠀⠈⠻⣿⣿⣦⠀⠀⠀
;; ⠀⠀⣼⣿⣿⠋⠀⠀⠀⠀⠀⠛⠛⢻⣿⣿⡀⠀⠀⠀⠀⠀⠀⠀⠙⣿⣿⣧⠀⠀
;; ⠀⢸⣿⣿⠃⠀⠀⠀⠀⠀⠀⠀⠀⢀⣿⣿⣷⠀⠀⠀⠀⠀⠀⠀⠀⠸⣿⣿⡇⠀
;; ⠀⣿⣿⡿⠀⠀⠀⠀⠀⠀⠀⠀⢀⣾⣿⣿⣿⣇⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⣿⠀
;; ⠀⣿⣿⡇⠀⠀⠀⠀⠀⠀⠀⢠⣿⣿⡟⢹⣿⣿⡆⠀⠀⠀⠀⠀⠀⠀⣹⣿⣿⠀
;; ⠀⣿⣿⣷⠀⠀⠀⠀⠀⠀⣰⣿⣿⠏⠀⠀⢻⣿⣿⡄⠀⠀⠀⠀⠀⠀⣿⣿⡿⠀
;; ⠀⢸⣿⣿⡆⠀⠀⠀⠀⣴⣿⡿⠃⠀⠀⠀⠈⢿⣿⣷⣤⣤⡆⠀⠀⣰⣿⣿⠇⠀
;; ⠀⠀⢻⣿⣿⣄⠀⠀⠾⠿⠿⠁⠀⠀⠀⠀⠀⠘⣿⣿⡿⠿⠛⠀⣰⣿⣿⡟⠀⠀
;; ⠀⠀⠀⠻⣿⣿⣧⣄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⣾⣿⣿⠏⠀⠀⠀
;; ⠀⠀⠀⠀⠈⠻⣿⣿⣷⣤⣄⡀⠀⠀⠀⠀⠀⠀⢀⣠⣴⣾⣿⣿⠟⠁⠀⠀⠀⠀
;; ⠀⠀⠀⠀⠀⠀⠈⠛⠿⣿⣿⣿⣿⣿⣶⣶⣿⣿⣿⣿⣿⠿⠋⠁⠀⠀⠀⠀⠀⠀
;; ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠉⠛⠛⠛⠛⠛⠛⠉⠉⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀

"♥️ Hello Chat! ♥️"
