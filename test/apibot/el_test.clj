(ns apibot.el-test
  "Tests for apibot.el"
  (:refer-clojure :exclude [resolve])
  (:require [presto.core :refer [expected-when]]
            [apibot.el :refer [resolve]]))

(expected-when
 resolve-test
 resolve
 :when [{} ""] = ""
 :when [{} "foo"] = "foo"
 :when [{} "foo/bar{}/!baz"] = "foo/bar{}/!baz"

  ;; Handling of absent values in the scope
 :when [{} "{{api}}"] = ""
 :when [{:api 1} "{{foo}}"] = ""

 ;; No escaping
 :when [{:name "A" :other-name "B"} "Hello {{name}} & {{other-name}}"] = "Hello A & B"
 :when [{:x "A & B"} "Hello {{x}}"] = "Hello A & B"
 :when [{:x "'\"<>"} "Hello {{x}}"] = "Hello '\"<>"

  ;; Simple Replacements
 :when [{:api 1} "{{api}}"] = "1"
 :when [{:api 1} "/foo/{{api}}/bar"] = "/foo/1/bar"

  ;; Nested Scope
 :when [{:config {:api 1}} "/{{config.api}}/"] = "/1/"
 :when [{:a {:b {:c 1}}} "/{{a.b.c}}/"] = "/1/"
 :when [{:a {:b {:c 1}}} "/{{a.b.c.d.e.f.g}}/"] = "//"

 ;; Objects
 :when [{:api 1} {}] = {}
 :when [{:api 1} []] = []
 :when [{:api 1} [1 2 3]] = [1 2 3]
 :when [{:api 1} {:a {:b "template{{api}}"}}] = {:a {:b "template1"}}
 :when [{:api 1} {:a {:b ["template{{api}}"]}}] = {:a {:b ["template1"]}})
