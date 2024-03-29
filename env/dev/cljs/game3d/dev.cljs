(ns game3d.dev
  (:require [game3d.core :as core]
            [figwheel.client :as figwheel :include-macros true]
            [weasel.repl :as weasel]))

(enable-console-print!)

(figwheel/watch-and-reload
  :websocket-url "ws://localhost:3449/figwheel-ws"
  :jsload-callback (fn [] (core/init!)))

(weasel/connect "ws://localhost:9001" :verbose true)

(core/init!)
