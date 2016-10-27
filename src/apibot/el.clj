(ns apibot.el
  "el (expression language) a simple language for interacting with the
  context."
  (:refer-clojure :exclude [resolve])
  (:require [clojure.walk :refer [postwalk-replace]]
            [apibot.util.ex :refer [error]]
            [instaparse.core :as insta]))

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
  (let [parsed (insta/parses el-parser string)]
    (cond
      (= (count parsed) 0)
        {:error (str "Unable to parse '" string "'")}
      (> (count parsed) 1)
        {:error (str "Ambiguous grammar!")}
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
              [] (first parsed))))))

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
    => {:url 'api/1/fop'}"
  [scope obj]
  (postwalk-replace (fn [x]
                      (if (string? x)
                        (resolve-str scope)
                        x))
                    obj))


