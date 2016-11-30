(ns apibot.nodes.request-test
  (:require [apibot.nodes.request :refer :all]
            [apibot.graph :refer [execute!]]
            [clojure.test :refer [deftest is]]
            [presto.core :refer [expected-when]]))

(def exec-node! #(execute! % {}))

(defn has-error?
  [error-regex scope]
  (->> (get-in scope [:$http-errors :errors])
       first
       :message
       (re-find error-regex)
       some?))

(deftest test-http-request-validation
  (is (->>
       (http-request-node
        {:name "missing url"
         :request-template {:method :get
                            :headers {}}})
       exec-node!
       (has-error? #":url")))

  (is (->>
       (http-request-node
        {:name "missing url"
         :request-template {:method :get :url "http://url.com"}})
       exec-node!
       (has-error? #":headers"))))

(deftest test-http-request-node-execution
  (is (->>
       (http-request-node
        {:name "Make a request to google"
         :request-template {:method :get
                            :url "http://google.com"
                            :headers {}}})
       exec-node!
       keys
       (= [:$response])))

  (is (->>
       (http-request-node
        {:name "Make a request with resolving scope attrs"
         :request-template {:method :get
                            :headers {}
                            :url "{{root}}/foo"}})
       (#(execute! % {:root "http://google.com"}))
       keys
       (= [:root :$response])))

  (is (->>
       (http-request-node
        {:name "Make a malformed request"
         :request-template {:method :get
                            :url "http://a n *&^ 1"
                            :headers {}}})
       exec-node!
       keys
       (= [:$http-errors])))

  (is (->>
       (http-request-node
        {:name "make a request that fails"
         :request-template {:method :get
                            :url "http://some-url-out-there.nl"
                            :headers {}}})
       exec-node!
       keys
       (= [:$http-errors]))))
