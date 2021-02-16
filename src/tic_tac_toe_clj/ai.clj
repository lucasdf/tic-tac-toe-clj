(ns tic-tac-toe-clj.ai
  (:require [clojure.zip :as zip]))

(def state
  {:grid {[0 0] {:player :circle}
          [0 1] nil
          [0 2] nil
          [1 0] nil
          [1 1] nil
          [1 2] nil
          [2 0] nil
          ;[2 1] nil
          ;[2 2] nil
          }})

(def
  start-zipper
  (->>
    (zip/zipper
      (comp not nil? :children)
      :children
      (fn [node children]
        (assoc node :children (vec children)))
      (assoc state
        :children []))
    ))

;; generates all 255168 game permutations considering that start-zipper contains a single first movement
;; todo - make this lazy
;; todo - shouldn't generate new permutations when a given path already met the winning condition
;;        nodes that generate a winning condition must be a leaf
;; todo - perform a breath-first search over the lazy search-space to find the shortest path
;;        that provides a winning condition

(def
  search-space
  (loop [loc start-zipper
         player :circle]
    (let [node (zip/node loc)]
      (cond
        (zip/end? loc)
        loc

        (nil? node)
        (recur (zip/next loc) player)

        :else
        (let [new-nodes (->> node
                             :grid
                             (filter (fn [[k v]] (nil? v)))
                             (into {})
                             (map (fn [[k v]]
                                    (-> node
                                        (assoc-in [:grid k :player] player)
                                        (dissoc :children)
                                        (assoc :children []))))
                             (filter #(tic-tac-toe-clj.core/check-winner % [-1 -1] player))
                             )]
          (recur (if (not-empty new-nodes)
                   (->>
                     (reduce (fn [acc val]
                               (zip/append-child acc val))
                             loc
                             new-nodes)
                     zip/next
                     )
                   (zip/next loc))
                 (if (= player :circle) :cross :circle)))))
    ))

(->>
  (zip/zipper
    (comp not nil? :children)
    :children
    (fn [node children]
      (assoc node :children (vec children)))
    (-> search-space zip/root))
  zip/down
  zip/right
  zip/down
  )
