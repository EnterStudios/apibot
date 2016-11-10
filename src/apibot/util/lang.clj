(ns apibot.util.lang
  "Extensions to clojure core"
  (:require [clojure.pprint :refer [pprint]]))

(defn spy
  "Pretty print x and return x, useful for debugging"
  [x]
  (pprint x)
  x)

(defn rand-item
  "Returns a random item from coll"
  [coll]
  (nth (vec coll)
       (rand-int (count coll))))
