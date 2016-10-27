(ns apibot.graph
  "The execution graph"
  (:require [apibot.request :as request]
            [apibot.el :as el]))

(defrecord ExecutionGraph [init])

(defn make-graph
  [init]
  (->ExecutionGraph init))

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

(defn execute-graph!
  [graph session]
  (loop [node (:init graph)
         session session
         history [{:session session :node node}]]
    (let [resulting-session (execute! node session)
          succesor? (succesor? node resulting-session)]
      (if (not succesor?)
        history
        (recur succesor?
               resulting-session
               (conj history {:session resulting-session :node node}))))))


(defn map->AbstractNode
  [{:keys [successors consumer successor-predicate name]}]
  {:pre [(vector? successors) (fn? consumer) (fn? successor-predicate) (string? name)]}
  (println successors)
  (->AbstractNode successors consumer successor-predicate name))

(defn void-consumer [node session] session)

(defn always-first-successor [node session]
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
  [{:keys [name successors request-template]}]
  (map->AbstractNode
    {:successors successors
     :consumer (fn [this scope]
                 (let [response (->> (el/resolve request-template scope)
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
  [{:keys [name extractor successors]}]
  (map->AbstractNode
    {:successors successors
     :consumer (fn [this scope]
                 (extractor scope))
     :successor-predicate always-first-successor
     :name name}))

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
