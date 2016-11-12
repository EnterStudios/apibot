(ns apibot.graph
  "The execution graph"
  (:require [apibot.request :as request]
            [apibot.el :as el]))

(defrecord ExecutionGraph [init])

(def make-graph ->ExecutionGraph)

(defn make-linear-graph
  "Creates a new graph given a set of nodes where the successor of the first node
  is the second node and so on."
  [& nodes]
  (loop [remaining-nodes (reverse nodes)
         graph (last nodes)]
    (if (empty? remaining-nodes)
      (->ExecutionGraph graph)
      (let [node (first remaining-nodes)]
        (recur (rest remaining-nodes)
               (assoc node :successors [graph]))))))

(defprotocol Node
  (execute! [this session])
  (successors [this])
  (succesor? [this session]))

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

  (execute! [this session]
    (consumer this session))

  (successors [this] (:successors this))

  (succesor? [this session]
    (successor-predicate this session)))

(defn map->AbstractNode
  [{:keys [successors consumer successor-predicate name]}]
  {:pre [(vector? successors)
         (fn? consumer)
         (fn? successor-predicate)
         (string? name)]}
  (->AbstractNode successors consumer successor-predicate name))

(defn- void-consumer [node session] session)

(defn- always-first-successor [node session]
  (first (:successors node)))

(defn initialization-node
  [{:keys [successors]}]
  (map->AbstractNode
   {:successors successors
    :consumer void-consumer
    :successor-predicate always-first-successor
    :name "Initialization"}))

(defn termination-node
  []
  (map->AbstractNode
   {:successors []
    :consumer void-consumer
    :successor-predicate (fn [this session] nil)
    :name "Termination"}))

(defn http-request-node
  "A node which executes by making an http request

  Arguments
  - name: the node's name
  - successors: a list of successors
  - request-template: a map describing the request, see example.

  Example:
  (http-request-node
    {:name 'get google'
     :successors []
     :request-template {:method :get
                        :url 'https://google.com'}})

  (http-request-node
    {:name 'get google'
     :successors []
     :request-template {:method :get
                        :url 'https://google.com'}})
  "
  [{:keys [name successors request-template]}]
  (map->AbstractNode
   {:successors successors
    :consumer (fn [this scope]
                (let [response (->> (el/resolve scope request-template)
                                    (request/make!))]
                  (assoc scope :$response response)))
    :successor-predicate always-first-successor
    :name name}))

(defn assertion-node
  [{:keys [name assertion message-template successors]}]
  (map->AbstractNode
   {:successors successors
    :consumer (fn [this scope]
                (if (assertion this scope)
                  scope
                  (assoc scope :$assertion-failed
                         {:message (el/resolve scope message-template)})))
    :successor-predicate (fn [this scope]
                           (if (contains? scope :$assertion-failed)
                             nil (first (:successors this))))
    :name name}))

(defn extractor-node
  "Arguments
  - name: the name of this extractor
  - extractor: a fn scope -> scope
  - successors: the successors to this node"
  [{:keys [name extractor successors]}]
  (map->AbstractNode
   {:successors successors
    :consumer (fn [this scope]
                (extractor scope))
    :successor-predicate always-first-successor
    :name name}))

(defn extractor-header-node
  [{:keys [name header-name as successors]}]
  (extractor-node
   {:name name
    :successors successors
    :extractor (fn [scope]
                 (let [header-value (get-in scope [:$response
                                                   :headers
                                                   header-name])]
                   (assoc scope as header-value)))}))

(defn extractor-body-node
  [{:keys [name extractor as successors]}]
  (extractor-node
   {:name name
    :successors successors
    :extractor (fn [scope]
                 (let [body (get-in scope [:$response :body])
                       extracted (extractor body)]
                   (assoc scope as extracted)))}))

(defn branching-node
  [{:keys [name successor-predicate successors]}]
  (map->AbstractNode
   {:successors successors
    :consumer void-consumer
    :successor-predicate successor-predicate
    :name name}))

(defn mock-node
  [{:keys [name successor]}]
  (map->AbstractNode
   {:successors [successor]
    :consumer void-consumer
    :successor-predicate always-first-successor
    :name name}))

(defn execute-graph!
  [graph session]
  (loop [node (:init graph)
         session session
         history [{:session session :node (initialization-node node)}]]
    (let [resulting-session (execute! node session)
          succesor? (succesor? node resulting-session)]
      (if (not succesor?)
        (conj history {:session resulting-session :node node})
        (recur succesor?
               resulting-session
               (conj history {:session resulting-session :node node}))))))

(defn execute-node!
  "Executes a graph with a single node on it.
  Equivalent to calling execute-graph! with a single node graph."
  [node session]
  (execute-graph! (make-graph node) session))
