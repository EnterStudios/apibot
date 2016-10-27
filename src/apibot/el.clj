(ns apibot.el
  "el (expression language) a simple language for interacting with the
  context."
  (:refer-clojure :exclude [resolve])
  (:require [clojure.walk :refer [postwalk]]
            [apibot.util.ex :refer [error try-maybe maybe-not]]
            [instaparse.core :as insta]))

(defrecord ElError [message])

(def el-parser (insta/parser
  "
  <EL>            := TOKEN+
  <TOKEN>         := STRING | INTERPOLATION
  INTERPOLATION   := <'#'> <'{'> SYMBOL (<'.'> SYMBOL)* <'}'>
  STRING          := #'\\A[^#]+'
  <SYMBOL>        := #'\\A\\w+'
  "))

(defn resolve-str
  [scope string]
  (if (empty? string)
    string
    (let [parsed (insta/parses el-parser string)]
      (cond
        (= (count parsed) 0)
          (->ElError (str "Unable to parse '" string "'"))
        (> (count parsed) 1)
          (->ElError (str "Ambiguous grammar!: " parsed))
        :else
        (apply str (reduce (fn [result [token-id & contents]]
                  (cond
                    (= :STRING token-id)
                      (into result contents)
                    (= :INTERPOLATION token-id)
                      (->> (map keyword contents)
                           (get-in scope)
                           (conj result))
                    :else
                      (error "Unknown token id: " token-id)))
                [] (first parsed)))))))

(defn resolve
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
                   (if (string? x)
                     (let [el-result (resolve-str scope x)]
                       (if (error? el-result)
                         (maybe-not el-result)
                         el-result))
                     x)
                   ) obj))))

(defn error?
  "Returns true if x is an el error"
  [x]
  (instance? ElError x))
