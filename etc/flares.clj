(tagged-literal 'flare/html
                {:url "https://calva.io"
                 :title "Calva homepage"
                 :key "example"})

(tagged-literal 'flare/html
                {:html "<h1>Hello, Nubank!</h1>"
                 :title "Sidebar Display"})

(tagged-literal
 'flare/html
 {:html (->> [0 1]
             (iterate (fn [[a b]] [b (+ a b)]))
             (map first)
             (take 50)
             (map #(str "<li>" % "</li>"))
             (apply str)
             (format "<ol>%s</ol>"))
  :title "Fibonacci"
  :key "example"
  :sidebar-panel? true})

(tagged-literal
 'flare/html
 {:html
  "<style>
     body { margin: 0; background: #0a0a2e; overflow: hidden; }
     svg { width: 100vw; height: 100vh; }
     .ring { fill: none; stroke-width: 2; transform-origin: center; }
     @keyframes spin { to { transform: rotate(360deg); } }
     @keyframes pulse { 0%,100% { opacity: .3; } 50% { opacity: 1; } }
     @keyframes dash { to { stroke-dashoffset: 0; } }
   </style>
   <svg viewBox='-200 -200 400 400'>
     <defs>
       <radialGradient id='glow'>
         <stop offset='0%' stop-color='#ff006e'/>
         <stop offset='100%' stop-color='transparent'/>
       </radialGradient>
     </defs>
     <circle r='8' fill='#ff006e' style='animation: pulse 2s infinite'/>
     <circle r='30' fill='url(#glow)' opacity='.4'/>
     <circle class='ring' r='40'  stroke='#00f5d4' stroke-dasharray='12 8'
             style='animation: spin 6s linear infinite'/>
     <circle class='ring' r='70'  stroke='#fee440' stroke-dasharray='20 10'
             style='animation: spin 10s linear infinite reverse'/>
     <circle class='ring' r='100' stroke='#9b5de5' stroke-dasharray='30 15'
             style='animation: spin 14s linear infinite'/>
     <circle class='ring' r='130' stroke='#00bbf9' stroke-dasharray='8 25'
             style='animation: spin 8s linear infinite reverse'/>
     <circle class='ring' r='160' stroke='#f15bb5' stroke-dasharray='50 10'
             stroke-dashoffset='200'
             style='animation: spin 20s linear infinite, dash 4s ease-in-out infinite alternate'/>
     <g style='animation: spin 12s linear infinite reverse'>
       <circle cx='100' cy='0' r='4' fill='#00f5d4' style='animation: pulse 1.5s infinite'/>
       <circle cx='-70' cy='70' r='3' fill='#fee440' style='animation: pulse 2.5s infinite .5s'/>
       <circle cx='0' cy='-130' r='5' fill='#9b5de5' style='animation: pulse 1.8s infinite 1s'/>
     </g>
   </svg>"
  :title "Orbits"
  :key "example"})
