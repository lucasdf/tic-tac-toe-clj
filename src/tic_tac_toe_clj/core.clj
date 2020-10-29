(ns tic-tac-toe-clj.core
  (:require [quil.core :as q :include-macros true]))

(def width 500)
(def height 500)
(def margin-bottom 50)

(defn setup []
  {:grid {[0 0] nil [0 1] nil [0 2] nil
          [1 0] nil [1 1] nil [1 2] nil
          [2 0] nil [2 1] nil [2 2] nil}
   :player :circle
   :winner nil
   :winning-check {:row [0 0 0] :column [0 0 0] :diag-left [0 0 0] :diag-right [0 0 0]}})

(defn coordinate->grid [x y]
  [(cond
     (< y (* height 0.333M)) 0
     (< y (* height 0.663M)) 1
     (< y (* height 0.993M)) 2
     :else -1)
   (cond
     (< x (* width 0.333M)) 0
     (< x (* width 0.666M)) 1
     (< x (* width 0.999M)) 2
     :else -1)])

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
    (cond
      (= (get-in #spy/p state [:winning-check :row x]) sum-to-win)
      {:row x}
      (= (get-in state [:winning-check :column y]) sum-to-win)
      {:column y}
      (= (reduce #(+ %1 %2) 0 (get-in state [:winning-check :diag-left])) sum-to-win)
      {:diag-left true}
      (= (reduce #(+ %1 %2) 0 (get-in state [:winning-check :diag-right])) sum-to-win)
      {:diag-right true}
      :else false)))

(defn click-target [{:keys [player winner] :as state} x y]
  (let [player-code (player->code player)]
    (or (when-let [[grid-x grid-y :as target] (coordinate->grid x y)]
          (when (and (not winner) (not= grid-x -1) (not= grid-y -1) (not (get-in state [:grid target :player])))
            (-> state
                (assoc-in [:grid target] {:player player})
                (assoc :player (next-player player))
                (update-in [:winning-check :row grid-x] + player-code)
                (update-in [:winning-check :column grid-y] + player-code)
                (assoc-in-if [:winning-check :diag-left grid-x] (and (= grid-x grid-y) player-code))
                (assoc-in-if [:winning-check :diag-right grid-y] (and (= (+ grid-x grid-y 1) 3) player-code))
                ((fn [s]
                   (if-let [winner-position (check-winner s target player)]
                     (-> s
                         (assoc-in-if [:winner] player)
                         (assoc-in-if [:winner-position] winner-position))
                     s))))))
        state)))

(defn update-state [state]
  (if (q/mouse-pressed?)
    (click-target state (q/mouse-x) (q/mouse-y))
    state))

(defn draw-board []
  (q/line (* width 0.333M) 0 (* width 0.333M) height)
  (q/line (* width 0.666M) 0 (* width 0.666M) height)
  (q/line  0 (* height 0.333M)  width (* height 0.333M))
  (q/line  0 (* height 0.666M)  width (* height 0.666M))
  (q/line  0 height  width height))

(defn draw-status-bar [{:keys [winner player]}]
  (q/fill 0 0 0)
  (q/text-size 20)
  (if winner
    (q/text (str (name winner) " has won") 175 (+ height 30))
    (do
      (q/text (str "Current player: " (name player)) 20 (+ height 30))
      (q/text (str (int (/ (q/millis) 1000)) "s") (- width 50) (+ height 30)))))

(defn draw-marks [state]
  (doseq [[id grid] (:grid state)]
    (when (:player grid)
      (mark-grid (:player grid) id))))

(defn draw [state]
  (q/background 255)
  (draw-board)
  (draw-status-bar state)
  (draw-marks state))
