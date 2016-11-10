(ns apibot.el
  "el (expression language) a simple language for interacting with the
  context."
  (:refer-clojure :exclude [resolve])
  (:require [clojure.walk :refer [postwalk]]
            [apibot.util.ex :refer [error try-maybe maybe-not]]
            [clostache.parser :refer [render]]))

(defn resolve-str
  [scope string]
  (if (empty? string)
    string
    (let [modified-template (clojure.string/replace string #"\{\{(.*?)\}\}" "{{{$1}}}")]
      (render modified-template scope))))

(defn resolve
  "Given a scope and a map as arguments, resolve will traverse
  all values in the map that are expressions and resolve them
  according to the scope. An error will be returned if there
  is no mapping for the given expression inside the scope.

  Examples:
  (resolve {} 'foo') => 'foo'
  (resolve {:api 1} 'api/#{api}/foo') => 'api/1/foo'
  (resolve {:root 'http://foo.com' :api 4} '{root}/api/{api}/bar')
    => 'http://foo.com/api/4/bar'
  (resolve {:api 1} {:url 'api/{api}/fop'})
    => {:url 'api/1/fop'}

  Error Handling:
  It can be the case that the template is malformed in which case
  an ElError will be returned. You should always verify that the return
  of this function was an error using the error? function.

  Example:
  (error? (resolve {} '#{}')) => true
  "
  [scope obj]
  {:pre [(or (string? obj) (coll? obj))]
   :post [(or (string? obj) (string? %)
              (coll? obj) (coll? %))]}
  (if (string? obj)
    (resolve-str scope obj)
    (try-maybe
     #(postwalk (fn [x]
                  (if (string? x) (resolve-str scope x) x))
                obj))))
