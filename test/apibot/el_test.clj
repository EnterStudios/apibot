(ns apibot.el-test
  "Tests for apibot.el"
  (:require [presto.core :refer [expected-when]]
            [apibot.el :refer [render]]))

(expected-when
 render-test
 render
 :when [{} ""] = ""
 :when [{} "foo"] = "foo"
 :when [{} "foo/bar{}/!baz"] = "foo/bar{}/!baz"

  ;; Syntax errors
 :when [{} "$api"] = {:error "unexpected token after $"}
 :when [{} "$ pi"] = {:error "unexpected token after $"}
 :when [{} "$ pi$"] = {:error "unexpected token after $"}
 :when [{} "${ pi$"] = {:error "unexpected token after ${"}

  ;; Error handling
 :when [{} "${api}"] = {:error "unknown key api"}
 :when [{:api 1} "${foo}"] = {:error "unknown key foo"}

  ;; Simple Replacements
 :when [{:api 1} "${api}"] = "1"
 :when [{:api 1} "/foo/${api}/bar"] = "/foo/1/bar"

  ;; Nested
 :when [{:config {:api 1}} "/${config.api}/"] = "/1/")
