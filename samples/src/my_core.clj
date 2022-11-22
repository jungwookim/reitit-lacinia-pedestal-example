(ns my-core
  (:gen-class)
  (:require [com.walmartlabs.lacinia.pedestal2 :as lp]
            [com.walmartlabs.lacinia.schema :as schema]
            [my-server :as router]
            [io.pedestal.http :as http]
            [reitit.pedestal :as pedestal]))

(def hello-schema
  (schema/compile
   {:queries {:hello {:type    'String
                      :resolve (constantly "world")}}}))

(def service2 (lp/default-service hello-schema (-> {:env                 :dev
                                                    :port                3000
                                                    ::http/type           :jetty
                                                    ::http/join?          false
                                                    ::http/secure-headers {:content-security-policy-settings {:default-src "'self'"
                                                                                                              :style-src   "'self' 'unsafe-inline'"
                                                                                                              :script-src  "'self' 'unsafe-inline'"}}})))

(-> service2
    (http/default-interceptors)
    (update ::http/interceptors conj {})
    (pedestal/replace-last-interceptor router/router)
    (http/dev-interceptors)
    (http/create-server)
    (http/start))

(comment
  (require '[gniazdo.core :as ws])
  (def socket
    (ws/connect
     "ws://localhost:3000/ws"
     :on-receive #(prn 'received %)))
  (ws/send-msg socket "hello")
  (ws/close socket)
  )