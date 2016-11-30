(ns apibot.util.validators
  "Mostly validators to verify that things are what they should be")

(defn url?
  [url]
  (try (and (new java.net.URL url) true)
       (catch Exception e false)))
