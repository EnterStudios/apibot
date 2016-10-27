(ns apibot.util.json
  "Namespace for dealing with JSON (de)serialization."
  (:require
   [apibot.util.ex :refer [error]]
   [clojure.string :as s]
   [clojure.data.json :as json]))

(defn- parse-key
  [string]
  (->> (s/split string #"_")
       (s/join "-")
       (keyword)))

(defn- write-key
  [x]
  (cond
    (number? x) (str x)
    (string? x) x
    (keyword? x) (s/replace (name x) #"-" "_")
    :else (error "Unknown key type for " x)))

(defn parse
  "Parses a JSON string into clojure datastructures"
  [string]
  {:pre [(string? string)]
   :post [(some? %)]}
  (json/read-str string :key-fn parse-key))

(defn write
  "Serializes a clojure object into a JSON string"
  [obj]
  {:pre [(coll? obj)]
   :post [(string? %)]}
  (json/write-str obj :key-fn write-key))

