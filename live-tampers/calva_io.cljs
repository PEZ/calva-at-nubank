(ns calva-io)

(defonce _inject-dark-css
  (let [style-el (js/document.createElement "style")]
    (set! (.-id style-el) "epupp-darkmode")
    (set! (.-textContent style-el)
          "body.epupp-dark {
             --md-default-bg-color: #1a1a2e !important;
             --md-default-fg-color: #e0e0e0 !important;
             --md-typeset-color: #e0e0e0 !important;
             --md-code-bg-color: #16213e !important;
             --md-code-fg-color: #a8dadc !important;
             background-color: #1a1a2e !important;
             color: #e0e0e0 !important;
           }
           body.epupp-dark .md-header {
             background-color: #0f3460 !important;
           }
           body.epupp-dark .md-nav {
             background-color: #1a1a2e !important;
           }
           body.epupp-dark .md-sidebar {
             background-color: #1a1a2e !important;
           }")
    (.appendChild js/document.head style-el)))

(defn toggle-dark-mode! []
  (let [cl (.-classList js/document.body)]
    (if (.contains cl "epupp-dark")
      (do (.remove cl "epupp-dark") "☀️ Light mode")
      (do (.add cl "epupp-dark") "🌙 Dark mode"))))

(defn search! [query]
  (let [input (js/document.querySelector ".md-search__input")
        toggle (js/document.querySelector "[data-md-toggle=search]")]
    (.focus input)
    (set! (.-value input) query)
    (.dispatchEvent input (js/Event. "focus" #js {:bubbles true}))
    (.dispatchEvent input (js/Event. "keyup" #js {:bubbles true}))
    (.dispatchEvent input (js/InputEvent. "input" #js {:bubbles true :data query}))
    (set! (.-checked toggle) true)
    (.dispatchEvent toggle (js/Event. "change" #js {:bubbles true}))
    (str "Searching for '" query "'...")))

(defn close-search! []
  (when-let [toggle (js/document.querySelector "[data-md-toggle=search]")]
    (set! (.-checked toggle) false)
    (.dispatchEvent toggle (js/Event. "change" #js {:bubbles true}))
    "Search closed"))

(defn search-results []
  (into []
        (map-indexed (fn [i a]
                       {:i i
                        :title (some-> (.querySelector a "h1, h2") .-textContent)
                        :href (.-href a)}))
        (js/document.querySelectorAll ".md-search-result__link")))

(defn navigate! [url]
  (js/setTimeout #(set! (.-location js/window) url) 50)
  (str "Navigating to " url))

(defn go! [i]
  (navigate! (:href (nth (search-results) i))))

(comment
  (toggle-dark-mode!)

  (search! "jack-in")
  (mapv #(select-keys % [:i :title]) (search-results))
  (go! 0)
  (close-search!)

  (navigate! "https://calva.io/")
  :rcf)

