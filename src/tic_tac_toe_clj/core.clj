(ns tic-tac-toe-clj.core
  (:require [quil.core :as q :include-macros true]))

(def width 500)
(def height 500)

(defn grid
  "what a mess..."
  []
  (apply
   hash-map
   (interleave (partition 2
                          (interleave
                           (flatten (map #(repeat 3 %) (range 0 3)))
                           (flatten (repeat 3 (range 0 3)))))
               (repeat nil))))

(defn setup []
  {:grid {[0 0] nil [0 1] nil [0 2] nil
          [1 0] nil [1 1] nil [1 2] nil
          [2 0] nil [2 1] nil [2 2] nil}
   :player :circle})

(defn coordinate->grid [x y]
  [(cond
     (< y (* height 0.33M)) 0
     (< y (* height 0.66M)) 1
     (< y (* height 0.99M)) 2)
   (cond
     (< x (* width 0.33M)) 0
     (< x (* width 0.66M)) 1
     (< x (* width 0.99M)) 2)])

(defn grid->center [[x y]]
  [(* (* width 0.33M 0.5M) (inc (* y 2)))
   (* (* height 0.33M 0.5M) (inc (* x 2)))])

(defn grid->cross [[x y]]
  [[(+ 0 (* height 0.33M y))
    (+ 0 (* width 0.33M x))
    (* height 0.33M (inc y))
    (* width 0.33M (inc x))]
   [(+ 0 (* height 0.33M y))
    (+ 0 (* width 0.33M (inc x)))
    (* height 0.33M (inc y))
    (* width 0.33M x)]])

(defmulti mark-grid
  (fn [player _] player))
(defmethod mark-grid :circle
  [_ grid]
  (let [[x y] (grid->center grid)]
    (q/fill 255 0 0)
    (q/ellipse x y 80 80)))
(defmethod mark-grid :cross
  [_ grid]
  (let [[[x1 y1 x2 y2] [x3 y3 x4 y4]] (grid->cross grid)]
    (q/stroke-weight 4)
    (q/line x1 y1 x2 y2)
    (q/line x3 y3 x4 y4)
    (q/stroke-weight 1)))

(defn next-player [player]
  (if (= player :circle) :cross :circle))

(defn click-target [state x y]
  (or (when-let [target (coordinate->grid x y)]
        (when-not (get-in state [:grid target :player])
          (-> state
              (assoc-in [:grid target] {:player (:player state)})
              (assoc :player (next-player (:player state))))))
      state))

(defn handle-click [state]
  (if (q/mouse-pressed?)
    (click-target state (q/mouse-x) (q/mouse-y))
    state))

(defn update-state [state]
  (handle-click state))

(defn draw-board []
  (q/line (* width 0.33M) 0 (* width 0.33M) height)
  (q/line (* width 0.66M) 0 (* width 0.66M) height)
  (q/line  0 (* height 0.33M)  width (* height 0.33M))
  (q/line  0 (* height 0.66M)  width (* height 0.66M)))

(defn draw-marks [state]
  (doseq [[id grid] (:grid state)]
    (when (:player grid)
      (mark-grid (:player grid) id))))

(defn draw [state]
  (q/background 255)
  (draw-board)
  (draw-marks state))
