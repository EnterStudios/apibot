(ns apibot.util.collections
  "Anything here should be considered an extension to clojure collections"
  (:require [clojure.walk :refer [postwalk-replace
                                  postwalk]]))

(defn map-keys
  "map-keys maps all keys in m using the given function f"
  [f m]
  {:pre [(fn? f) (map? m)]
   :post [(and "expected response to be a map" (map? %))]}
  (reduce (fn [reduction [k v]]
            (assoc reduction (f k) v))
          {} m))

(defn map-vals
  "map-vals maps all values in m using the given function f"
  [f m]
  {:pre [(fn? f) (map? m)]
   :post [(map? %)]}
  (reduce (fn [reduction [k v]]
            (assoc reduction k (f v)))
          {} m))

(defn all?
  [predicate coll]
  (reduce #(and %1 %2)
          true
          (map predicate coll)))
