(ns tic-tac-toe-clj.core
  (:require [quil.core :as q :include-macros true]))

(def width 500)
(def height 500)

(defn setup []
  {:grid {[0 0] nil [0 1] nil [0 2] nil
          [1 0] nil [1 1] nil [1 2] nil
          [2 0] nil [2 1] nil [2 2] nil}
   :player :circle
   :winner nil
   :winning-check {:row [0 0 0] :column [0 0 0] :diag-left [0 0 0] :diag-right [0 0 0]}})

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

(defn player->code [player]
  (if (= player :circle) 1 -1))

(defn assoc-in-if [m ks v]
  "Associates a truthy value in a nested associative structure"
  (-> m (cond-> v (assoc-in ks v))))

(defn check-winner [state [x y] player]
  (let [sum-to-win (* (player->code player) 3)]
    (when
     (or (= (get-in state [:winning-check :row x]) sum-to-win)
         (= (get-in state [:winning-check :column y]) sum-to-win)
         (= (reduce #(+ %1 %2) 0 (get-in state [:winning-check :diag-left])) sum-to-win)
         (= (reduce #(+ %1 %2) 0 (get-in state [:winning-check :diag-right])) sum-to-win))
      player)))

(defn click-target [{:keys [player] :as state} x y]
  (let [player-code (player->code player)]
    (or (when-let [[grid-x grid-y :as target] (coordinate->grid x y)]
          (when-not (get-in state [:grid target :player])
            (-> state
                (assoc-in [:grid target] {:player player})
                (assoc :player (next-player player))
                (update-in [:winning-check :row grid-y] + player-code)
                (update-in [:winning-check :column grid-y] + player-code)
                (assoc-in-if [:winning-check :diag-left grid-x] (and (= grid-x grid-y) player-code))
                (assoc-in-if [:winning-check :diag-right grid-y] (and (= (+ grid-x grid-y 1) 3) player-code))
                ((fn [s] (assoc-in-if s [:winner] (check-winner s target player)))))))
        state)))

(defn update-state [state]
  (if (q/mouse-pressed?)
    (click-target state (q/mouse-x) (q/mouse-y))
    state))

(defn draw-board []
  (q/line (* width 0.33M) 0 (* width 0.33M) height)
  (q/line (* width 0.66M) 0 (* width 0.66M) height)
  (q/line  0 (* height 0.33M)  width (* height 0.33M))
  (q/line  0 (* height 0.66M)  width (* height 0.66M)))

(defn draw-marks [state]
  (doseq [[id grid] (:grid state)]
    (when (:player grid)
      (mark-grid (:player grid) id))))

(defn draw-winner [winner]
  (when winner
    (q/text-size 30)
    (q/text (str (name winner) " has won") (* width 0.4M) (* height 0.4M))))

(defn draw [state]
  (q/background 255)
  (draw-board)
  (draw-marks state)
  (draw-winner (:winner state)))
