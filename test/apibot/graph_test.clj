(ns apibot.graph-test
  "Tests for apibot.graph"
  (:require [apibot.graph :refer :all]
            [presto.core :refer [expected-when]]))

(def empty-init-node (initialization-node {:successors []}))

(def login-graph
  (initialization-node {:successors [(mock-node {:name "authenticate"
                                                :successors []})]}))

(defn =-sessions
  [expected actual]
  (= (map :session expected)
     actual))

(expected-when execute-graph-test #(execute-graph! (make-graph %1) %2)
  :when [empty-init-node {}] =-sessions [{}]
  :when [login-graph {}] =-sessions [{} {}]
  :when [login-graph {:foo 1}] =-sessions [{:foo 1} {:foo 1}])


