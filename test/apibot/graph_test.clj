(ns apibot.graph-test
  "Tests for apibot.graph"
  (:require [apibot.mocks :refer [mock-server mock-server-url]]
            [apibot.nodes.assertions :refer [node-assert-http-status]]
            [apibot.nodes.request :refer [http-request-node]]
            [apibot.graph :refer :all]
            [mount.core :as mount]
            [clojure.test :refer [deftest is]]
            [presto.core :refer [expected-when]]))

(def node-init
  (extractor-node {:name "extract nothing"
                   :extractor (fn [scope] scope)}))

(def node-end (termination-node))
(def node-extract-token
  (extractor-node {:name "extract token"
                   :extractor (fn [scope] scope)}))
(def node-http
  (http-request-node
   {:name "get page"
    :request-template {:method :get
                       :headers {}
                       :url (mock-server-url)}}))
(def history
  (try
    (mount/start)
    (execute-graph!
     (make-linear-graph
      node-init
      node-http
      (node-assert-http-status {:status 200})
      node-extract-token
      node-end)
     {})
    (finally (mount/stop))))

(deftest verify-execution
  (is (= (count history) 6) (str "History: " (keys history))))
