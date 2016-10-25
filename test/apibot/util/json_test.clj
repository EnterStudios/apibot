(ns apibot.util.json-test
  "tests for apibot.util.json"
  (:require [presto.core :refer [expected-when]]
            [apibot.util.json :as json]))

(expected-when
 json-write-test json/write
 :when [{}] = "{}"
 :when [{:foo "bar"}] = "{\"foo\":\"bar\"}"
 :when [[]] = "[]"
 :when [[{} {}]] = "[{},{}]")

(expected-when
 json-parse-test json/parse
 :when ["{}"] = {}
 :when ["[]"] = []
 :when ["{\"foo\":1}"] = {:foo 1}
 :when ["{\"bar\": \"2\"}"] = {:bar "2"}
 :when ["{\"foo_bar\":[]}"] = {:foo-bar []}
 :when ["{\"foo_bar_baz\":[]}"] = {:foo-bar-baz []})
