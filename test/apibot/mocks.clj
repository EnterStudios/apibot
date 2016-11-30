(ns apibot.mocks
  "Holds many mock related functions"
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [mount.core :refer [defstate]]))

(defn app-handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello from Ring"})

(def server-config {:port 21388
                    :host "localhost"
                    :join? false})

(defn mock-server-url []
  (str "http://" (:host server-config) ":" (:port server-config)))

(defstate mock-server
  :start (let [server (run-jetty app-handler server-config)]
           (println "initializing server running on localhost")
           server)
  :stop (do (println "stopping server")
            (.stop mock-server)))
