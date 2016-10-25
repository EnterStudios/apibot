(ns apibot.nodes.extractors
  "Extractor namespace")

(defn extractor
  "Creates a new extractor"
  [extractor-name extractor-fun message]
  {:name extractor-name
   :extractor extractor-fun
   :message message})
