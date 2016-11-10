(ns apibot.util.ex
  "Exceptions")

(defn error
  "Shorthand method for throwing a runtime exception"
  [& messages]
  (throw (new RuntimeException (apply str messages))))

(defn maybe-not [value]
  (throw (ex-info "Maybe not" {:apibot.empty.value value})))

(defn try-maybe
  "Executes and returns f's value. If you call maybe-not inside f, the value
  of maybe-not will be returned instead."
  [f!]
  (try
    (f!)
    (catch clojure.lang.ExceptionInfo e
      (if-let [empty-value (:apibot.empty.value (ex-data e))]
        empty-value
        (throw e)))))
