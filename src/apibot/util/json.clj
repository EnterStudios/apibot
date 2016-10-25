(ns apibot.util.json
  "Json"
  (:require
   [apibot.util.ex :refer [error]]
   [clojure.string :as s]
   [clojure.data.json :as json]))

(defn parse-key
  [string]
  (->> (s/split string #"_")
       (s/join "-")
       (keyword)))

(defn write-key
  [x]
  (cond
    (number? x) (str x)
    (string? x) x
    (keyword? x) (s/replace (name x) #"-" "_")
    :else (error (str "Unknown key type for " x))))

(defn parse
  [string]
  (json/read-str string :key-fn parse-key))

(defn write
  [obj]
  {:pre [(not (nil? obj))]
   :post [(string? %)
          (not (nil? %))]}
  (json/write-str obj :key-fn write-key))
