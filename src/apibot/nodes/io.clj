(ns apibot.nodes.io
  (:require [apibot.util.json :as json]
            [apibot.el :as el]
            [apibot.graph :refer [map->AbstractNode always-first-successor]]))

(defn node-save-request
  "Creates a node which saves the body of the last HTTP request
  (via :$response) to the given file-name.

  Arguments:
  - file-name the path where the response will be written to. file-name supports
  el meaning you can write to '{{current-date}}.json'
  "
  [{:keys [file-name]}]
  (map->AbstractNode
   {:successors []
    :consumer (fn [this scope]
                (let [body (get-in scope [:$response :body])
                      resolved-file-name (el/resolve scope file-name)]
                  (spit resolved-file-name body))
                scope)
    :successor-predicate always-first-successor
    :name (str "Save request to " file-name)}))
