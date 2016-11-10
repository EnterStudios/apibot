(ns apibot.picnic
  (:require
   [apibot.graph :refer [execute-graph!
                         make-graph
                         extractor-node
                         http-request-node]]))

  (let [req-login (http-request-node
                   {:name "login"
                    :successors []
                    :request-template {:method :post
                                       :headers {:content-type "application/json"}
                                       :url "{{root}}/user/login"
                                       :body {:client-id 1
                                              :key "{{email}}"
                                              :secret "{{secret}}"}}})

        req-mystore (http-request-node
                     {:name "my store"
                      :successors []
                      :request-template {:method :get
                                         :url "{{root}}/my_store"}})

        req-cart-get (http-request-node
                      {:name "fetch cart"
                       :successors []
                       :request-template {:method :get
                                          :url "{{root}}/cart"}})

        req-cart-clear (http-request-node
                        {:name "clear cart"
                         :successors []
                         :request-template {:method :post
                                            :headers {:content-type "application/json"}
                                            :url "{{root}}/cart/clear"}})]
    (let [x (execute-graph!
             (make-graph req-login)
             {:root "https://gateway-nl-dev.picnicinternational.com/api/11"
              :email "foo2@bar.com"
              :secret "781e5e245d69b566979b86e28d23f2c7"})]
      (->> (last x)
           (:session)
           (:$response)
           (:body))))

