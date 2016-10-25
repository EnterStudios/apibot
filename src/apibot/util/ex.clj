(ns apibot.util.ex
  "Exceptions")

(defn error
  "Shorthand method for throwing a runtime exception"
  [message]
  (throw (new RuntimeException message)))
