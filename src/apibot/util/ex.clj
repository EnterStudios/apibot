(ns apibot.util.ex
  "Exceptions")

(defn error
  "Shorthand method for throwing a runtime exception"
  [& messages]
  (throw (new RuntimeException (apply str messages))))
