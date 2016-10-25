(ns apibot.el
  "el (expression language) a simple language for interacting with the
  context."
  (:import [java.javax ExpressionFactory])
  (:require [clojure.walk :refer [pre-walk]]))

(defn render
  "Given a scope and a map as arguments, render will traverse
  all values in the map that are expressions and render them
  according to the scope. An error will be returned if there
  is no mapping for the given expression inside the scope.

  Examples:
  (render {} 'foo') => 'foo'
  (render {:api 1} 'api/#{api}/foo') => 'api/1/foo'
  (render {:root 'http://foo.com' :api 4} '{root}/api/{api}/bar')
    => 'http://foo.com/api/4/bar'
  (render {:api 1} {:url 'api/{api}/fop'})
    => {:url 'api/1/fop'}"
  [scope obj]
  (pre-walk
   (fn [x] (if (string? x) (interpret-exp scope x) x)) obj))
