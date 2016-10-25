(ns apibot.nodes.request
  "Creation and execution of request nodes"
  (:require [org.httpkit.client :as http]
            [apibot.util.ex :refer [error]]
            [apibot.util :refer [map-keys map-vals]]
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

(defn- log-request [request]
  (println request)
  request)

(defn write-body-to-string
  [request]
  (let [body (:body request)]
    (if body
      (assoc request :body (json/write (:body request)))
      request)))

(defn write-headers-to-string
  [request]
  (->> (:headers request)
       (map-keys str)
       (map-vals str)
       (assoc request :headers)))

(defn parse-body
  [request]
  (let [body (:body request)]
    (if body
      (assoc request :body (json/parse (:body request)))
      request)))

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

(defn build
  [request]
  (->> (validate! request) ;; perform some validation over the request
       (write-body-to-string) ;; writes the :body to a JSON string
       (write-headers-to-string) ;; writers the headers to strings
       (append-options) ;; adds some default options
       (http/request) ;; creates a promise using http/request
       (deref) ;; derefs the promise
       (parse-body))) ;; parses the body back to a clojure map

(let [login-resp (build {:method :post
                         :url "https://gateway-nl-dev.picnicinternational.com/api/10/user/login"
                         :body {:client-id 1
                                :key "foo2@bar.com"
                                :secret "e807f1fcf82d132f9bb018ca6738a19f"}
                         :headers {"content-type" "application/json"}})
      token (get-in login-resp [:headers :x-picnic-auth])
      _ (println "token is..." (take 30 token))
      _ (assert (string? token))]

  (build {:method :get
          :url "https://gateway-nl-dev.picnicinternational.com/api/10/my_store"
          :headers {"x-picnic-auth" token}}))
