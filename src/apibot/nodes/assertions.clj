(ns apibot.nodes.assertions
  (:require [apibot.el :as el]
            [apibot.util.lang :refer [maybe-get! regex? xor]]
            [apibot.graph :refer [map->AbstractNode Node execute!]]
            [cats.monad.maybe :as maybe]))

(defn assertion-node
  "Creates an assertion node.

  Assertion nodes perform an assertion over the scope. If the assertion
  holds they continue their execution. If the assertion doesn't hold they
  append an error message to the scope with the :$assertion-failed key
  and stop the execution by returning no successors.

  Arguments
  - name: The name of the assertion
  - assertion: a function of node x scope -> boolean
    if the returns true, execution will continue. If false, an error message
    will be added as the :$assertion-failed key.
  - message-template: an EL template that will be used for reporting failed
    assertions.
  - successors: a set of possible successors.
  "
  [{:keys [name assertion message-template successors] :or {successors []}}]
  (map->AbstractNode
   {:name name
    :successors successors
    :consumer (fn [this scope]
                (if (assertion this scope)
                  scope
                  (assoc scope :$assertion-failed
                         {:message (el/resolve scope message-template)})))
    :successor-predicate (fn [this scope]
                           (if (contains? scope :$assertion-failed)
                             (maybe/nothing)
                             (maybe/seq->maybe (:successors this))))}))

(defn node-assert-http-status
  "Asserts the status of the last http request
  Arguments:
  - status: a number representing the expected status."
  [{:keys [status]}]
  {:pre [(number? status) (<= 200 status 599)]}
  (assertion-node
   {:name (str "Assert status " status)
    :assertion (fn [this scope]
                 (= (get-in scope [:$response :status]) status))
    :message-template (str "Expected response status to be " status " but was '{{$response.status}}'")}))

(defn node-assert-body-matches
  "Asserts that the body of the last http request matches some expectation
  Arguments:
  - regex: a regular expression. if passed the body is expected to match the given regular expression.
  - string: a string. if passed, the body is expected to match the given string."
  [{:keys [regex string]}]
  {:pre [(xor (string? string) (regex? regex))]}
  (assertion-node
   {:name (str "Assert body contains " (or regex string))
    :assertion (fn [this scope]
                 (let [body (get-in scope [:$response :body])]
                   (cond
                     (not body) false
                     string (.contains body string)
                     regex (re-find regex body))))
    :message-template (str "Expected response to match '" (or regex string) "' but was '{{$response.body}}'")}))
