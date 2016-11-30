(ns apibot.util.lang
  "Extensions to clojure core"
  (:require [cats.monad.maybe :as maybe]
            [clojure.pprint :refer [pprint]]))

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

(defn- maybe-if-present
  [x]
  (if (nil? x)
    (maybe/nothing)
    (maybe/just x)))

(defn maybe-get!
  [x]
  {:pre [(maybe/maybe? x)]
   :post [(some? %)]}
  (maybe/from-maybe x))

(defn regex?
  "true iff x is a regular expression"
  [x]
  (= (type x) (type #"")))

(defn xor
  "Returns a XOR b"
  [a b]
  (and (or a b)
       (not (and a b))))

(defn arg-count [f]
  "*has many limitations*

  This function returns the number of arguments on funciton f, post destructuring
  "
  {:pre [(instance? clojure.lang.AFunction f)]}
  (-> f class .getDeclaredMethods first .getParameterTypes alength))
