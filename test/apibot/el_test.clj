(ns apibot.el-test
  "Tests for apibot.el"
  (:refer-clojure :exclude [resolve])
  (:require [presto.core :refer [expected-when]]
            [apibot.el :refer [resolve error?]]))

(defn is-error?
  [expected actual]
  (= (error? expected) actual))

(expected-when
 resolve-test
 resolve
 :when [{} ""] = ""
 :when [{} "foo"] = "foo"
 :when [{} "foo/bar{}/!baz"] = "foo/bar{}/!baz"

  ;; Syntax errors
 :when [{} "#api"] is-error? true
 :when [{} "# pi"] is-error? true
 :when [{} "# pi#"] is-error? true
 :when [{} "#{ pi#"] is-error? true

  ;; Handling of absent values in the scope
 :when [{} "#{api}"] = ""
 :when [{:api 1} "#{foo}"] = ""

  ;; Simple Replacements
 :when [{:api 1} "#{api}"] = "1"
 :when [{:api 1} "/foo/#{api}/bar"] = "/foo/1/bar"

  ;; Nested Scope
 :when [{:config {:api 1}} "/#{config.api}/"] = "/1/"
 :when [{:a {:b {:c 1}}} "/#{a.b.c}/"] = "/1/"
 :when [{:a {:b {:c 1}}} "/#{a.b.c.d.e.f.g}/"] = "//"

 ;; Objects
 :when [{:api 1} {}] = {}
 :when [{:api 1} []] = []
 :when [{:api 1} [1 2 3]] = [1 2 3]
 :when [{:api 1} {:a {:b "template#{api}"}}] = {:a {:b "template1"}}
 :when [{:api 1} {:a {:b ["template#{api}"]}}] = {:a {:b ["template1"]}}

 ;; Nested failure
 :when [{} {:foo "template# {api}"}] is-error? true
 :when [{} {:a {:b ["template# {api}"]}}] is-error? true

 ;; Error handling
 :when [{} "foo"] is-error? false
 :when [{} "#"] is-error? true
 :when [{} "# { foo }"] is-error? true)


