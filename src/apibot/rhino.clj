(ns apibot.rhino
  (:import (org.mozilla.javascript Context Scriptable)))

(defn eval-js
  "Initializes a clean javascript environment, evaluates the given js-string in the javascript
  environment and returns the last result."
  [js-string]
  (let [context (Context/enter)]
    (try
      (let [scope (.initStandardObjects context)
            result (.evaluateString context scope js-string "<cmd>" 1 nil)]
        (Context/toString result))
      (finally (Context/exit)))))

(for [x (range 1000)]
  (do
    (time (eval-js "var fib = function(n) {
             if (n <= 1) {
               return 1;
             }
             return fib(n-1) + fib(n-2);
           }
           fib(10)"))
    (Thread/sleep 100)))
