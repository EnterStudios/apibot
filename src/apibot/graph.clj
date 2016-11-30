(ns apibot.graph
  "The execution graph"
  (:require [apibot.request :as request]
            [apibot.util.lang :refer [maybe-get! arg-count]]
            [cats.monad.maybe :as maybe]
            [apibot.el :as el]))

(defn make-linear-graph
  "Creates a new graph given a set of nodes where the successor of the first node
  is the second node and so on."
  [& nodes]
  {:pre [(and "at least one" (>= (count nodes) 1))
         (and "no duplicates" (= (count nodes) (count (set nodes))))]}
  (loop [remaining-nodes (rest (reverse nodes))
         graph (last nodes)]
    (if (empty? remaining-nodes)
      graph
      (let [node (first remaining-nodes)]
        (recur (rest remaining-nodes)
               (assoc node :successors [graph]))))))

(defprotocol Node
  "A Node is an immutable datastructure that represents a point in an execution graph.
  Nodes now 3 things:
  1. How to execute themselves (via the execute! function)
  2. Their statuc successors. A list of possible nodes that can follow after the
  execution of this.
  3. Their instance successor. A (maybe) Node that can follow after the execution
  of this."

  (execute!
    [this scope]
    "Executes a Node given the current scope.
    Arguments:
    - this: the node.
    - scope: the current scope.")

  (successors
    [this]
    "Returns a possibly empty list of possible successors for this node")

  (succesor
    [this scope]
    "Given the current scope, returns the successor to this node.
    The result is a (maybe/just) or (maybe/nothing) if there are no
    successors."))

(defrecord AbstractNode
           [;; A list of possible successor nodes that this node is aware of
            successors
   ;; A function (possibly with side effects)
            consumer
   ;; Successor predicate
            successor-predicate
   ;; The node's name
            name]
  Node

  (execute! [this scope]
    (consumer this scope))

  (successors [this] (:successors this))

  (succesor [this scope]
    (successor-predicate this scope)))

(defmethod clojure.core/print-method AbstractNode [x writer]
  (.write writer (str "Node(" (:name x) ")->" (:successors x))))

(defn map->AbstractNode
  [{:keys [successors consumer successor-predicate name] :or {successors []}}]
  {:pre [(vector? successors)
         (fn? consumer)
         (fn? successor-predicate)
         (= (arg-count consumer) 2)
         (= (arg-count successor-predicate) 2)
         (string? name)]}
  (->AbstractNode successors consumer successor-predicate name))

(defn- void-consumer [node scope] scope)

(defn always-first-successor [node scope]
  (maybe/seq->maybe (:successors node)))

(defn initialization-node
  [{:keys [successors] :or {successors []}}]
  (map->AbstractNode
   {:successors successors
    :consumer void-consumer
    :successor-predicate always-first-successor
    :name "Initialization"}))

(defn termination-node
  "A node that does nothing and has no successors"
  []
  (map->AbstractNode
   {:successors []
    :consumer void-consumer
    :successor-predicate always-first-successor
    :name "Termination"}))

(defn extractor-node
  "Arguments
  - name: the name of this extractor
  - extractor: a fn scope -> scope
  - successors: the successors to this node"
  [{:keys [name extractor successors] :or {successors []}}]
  {:pre [(= (arg-count extractor) 1)]}
  (map->AbstractNode
   {:successors successors
    :consumer (fn [this scope]
                (extractor scope))
    :successor-predicate always-first-successor
    :name name}))

(defn node-extract-header
  [{:keys [name header-name as successors] :or {successors []}}]
  (extractor-node
   {:name name
    :successors successors
    :extractor (fn [scope]
                 (let [header-value (get-in scope [:$response
                                                   :headers
                                                   header-name])]
                   (assoc scope as header-value)))}))

(defn node-extract-body
  [{:keys [name extractor as successors] :or {successors []}}]
  (extractor-node
   {:name name
    :successors successors
    :extractor (fn [scope]
                 (let [body (get-in scope [:$response :body])
                       extracted (extractor body)]
                   (assoc scope as extracted)))}))

(defn branching-node
  [{:keys [name successor-predicate successors] :or {successors []}}]
  (map->AbstractNode
   {:successors successors
    :consumer void-consumer
    :successor-predicate successor-predicate
    :name name}))

(defn execute-graph!
  "Executes a graph by recursively calling execute! and then
  executing the first successor from starting-node.
  This execution assumes that nodes produce no more than one successor.

  The execution finishes either when the current node has no more
  successors or if a loop is found.

  Arguments:
  - starting-node: a Node
  - scope: The initial scope.
  "
  [starting-node scope]
  (loop [visited? #{}
         node starting-node
         scope scope
         history [{:scope scope :node (initialization-node node)}]]
    (println "Executing node:" (:name node))
    (if (visited? node)
      history
      (let [resulting-scope (execute! node scope)
            maybe-succesor (succesor node resulting-scope)]
        (println "Successor: " maybe-succesor)
        (if (maybe/nothing? maybe-succesor)
          (conj history {:scope resulting-scope :node node})
          (recur (conj visited? node)
                 (maybe-get! maybe-succesor)
                 resulting-scope
                 (conj history {:scope resulting-scope :node node})))))))
