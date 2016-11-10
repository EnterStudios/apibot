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
