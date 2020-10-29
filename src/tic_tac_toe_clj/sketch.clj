(ns tic-tac-toe-clj.sketch
  (:require [quil.core :as q]
            [tic-tac-toe-clj.core :as core]
            [quil.middleware :as m]))

(q/defsketch ttc
  :host "host"
  :size [core/width (+ core/height core/margin-bottom)]
  :setup core/setup
  :update core/update-state
  :draw core/draw
  :middleware [m/fun-mode])

(defn -main [] ())
