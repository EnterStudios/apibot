(ns apibot.request
  "Creation and execution of request nodes"
  (:require [org.httpkit.client :as http]
            [apibot.util.ex :refer [error]]
            [apibot.util.collections :refer [map-keys map-vals]]
            [apibot.util.json :as json]))

(defn body? [request]
  (not (nil? (:body request))))

(defn get? [request]
  (= :get (:method request)))

(defn put? [request]
  (= :put (:method request)))

(defn post? [request]
  (= :post (:method request)))

(defn delete? [request]
  (= :delete (:method request)))

(defn url? [request]
  (not (nil? (:url request))))

(defn write-body-to-string
  [request]
  (let [body (:body request)]
    (if body
      (assoc request :body (json/write (:body request)))
      request)))

(defn write-headers-to-string
  [request]
  (->> (:headers request)
       (map-keys name)
       (map-vals str)
       (assoc request :headers)))

(defn parse-body
  [request]
  (let [body (:body request)]
    (cond (string? body)
          request
          (map? body)
          (assoc request :body (json/parse (:body request)))
          :else request)))

(defn append-options
  [request]
  (assoc request
         :as :text
         :timeout 5000))

(defn validate!
  [request]
  (assert (url? request))
  (when (get? request) (assert (not (body? request))))
  (when (post? request) (assert (body? request)))
  request)

(defn make!
  [request]
  (->> (validate! request) ;; perform some validation over the request
       (write-body-to-string) ;; writes the :body to a JSON string
       (write-headers-to-string) ;; writers the headers to strings
       (append-options) ;; adds some default options
       (http/request) ;; creates a promise using http/request
       (deref) ;; derefs the promise
       (parse-body))) ;; parses the body back to a clojure map
