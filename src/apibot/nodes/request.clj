(ns apibot.nodes.request
  (:require [apibot.util.collections :refer [all?]]
            [apibot.util.validators :refer [url?]]
            [apibot.graph :refer [map->AbstractNode always-first-successor]]
            [apibot.el :as el]
            [cats.monad.maybe :as maybe]
            [org.httpkit.client :as http]))

(defprotocol HttpResponse
  (request [this])
  (error? [this])
  (response? [this])
  (body [this])
  (headers [this])
  (status [this]))

(defrecord HttpResponseImpl
           [req res errors]
  HttpResponse
  (request [this] req)
  (error? [this] (not (empty? errors)))
  (response? [this] res)
  (body [this] (get res :body))
  (headers [this] (get res :headers))
  (status [this] (get res :status)))

(def request-validators
  [(fn [{:keys [method]}]
     (when-not (keyword? method)
       {:message ":method argument is missing"}))
   (fn [{:keys [method]}]
     (when-not (some #{:get :put :post :delete} [method])
       {:message "only :get, :put, :post and :delete are supported"}))

   (fn [{:keys [headers]}]
     (when-not (map? headers)
       {:message ":headers must be a map"}))
   (fn [{:keys [headers] :or {headers {}}}]
     (when-not (all? string? (keys headers))
       {:message ":headers keys must be a string"}))
   (fn [{:keys [headers :or {headers {}}]}]
     (when-not (all? string? (vals headers))
       {:message ":headers values must be a string"}))

   (fn [{:keys [url]}]
     (when-not (url? url)
       {:message (str ":url '" url "' is not well formed")}))

   (fn [{:keys [body method]}]
     (when (= method :get)
       (when body
         {:message "get requests should not contain a body"})))

   (fn [{:keys [body method]}]
     (when (= method :post)
       (when-not body
         {:message "post requests should contain a body"})))])

(defn- validate-request
  "Validates a request by returning a list of errors."
  [request]
  (->> (map (fn [validator] (validator request)) request-validators)
       (filter some?)))

(defn- execute-request!
  [request]
  (let [errors (validate-request request)]
    (if (not (empty? errors))
      (->HttpResponseImpl request nil errors)
      (let [response (deref (http/request request))
            error (:error response)]
        (if error
          (->HttpResponseImpl
           request nil
           [{:message "Runtime error when making request" :error error}])
          (->HttpResponseImpl
           request response []))))))

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
  [{:keys [name successors request-template] :or {successors []}}]
  (map->AbstractNode
   {:successors successors
    :consumer (fn [this scope]
                (let [request (el/resolve scope request-template)
                      http-response (execute-request! request)]
                  (if (error? http-response)
                    (assoc scope :$http-errors http-response)
                    (assoc scope :$response (response? http-response)))))
    :successor-predicate (fn [this scope]
                           (println "Finding first successor in "
                                    (:successors this))
                           (println "Errors: " (contains? scope :$http-errors))
                           (if (contains? scope :$http-errors)
                             (maybe/nothing)
                             (maybe/seq->maybe (:successors this))))
    :name name}))
