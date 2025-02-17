(ns server.api.requests
  (:require
    [cheshire.core :as json :refer [generate-string]]
    [clojure.string :as string]
    [ring.util.response :refer [response]]
    [taoensso.telemere :as t]
    [server.models.requests :as requests]))
(import [java.time Instant])

(defn get-timeframe-requests-by-proxy [req params & _]
  (t/log! :debug "Getting requests by timeframe")
  (let [context (:context req)
        query-params (:query-params req)
        start-time (java.time.Instant/parse (get query-params "start-time"))
        end-time (java.time.Instant/parse (get query-params "end-time"))
        status-code-group (when-let [code (get query-params "status-code-group")]
                             (when-not (string/blank? code)
                               (parse-long code)))
        requests (requests/get-timeframe-requests-by-proxy
                         {:workspace-id (-> context
                                            :user-preferences
                                            :selected_workspace)
                          :proxy-id (:proxy-id params)
                          :status-code-group status-code-group
                          :start-time start-time
                          :end-time end-time})]
    (response {:status "ok"
               :data {:total (apply + (map :total requests))
                      :status-code-group (if status-code-group
                                           (str status-code-group "xx")
                                           "all")
                      :list requests}})))

(defn get-requests-by-proxy-id [req params & _]
  (let [context (:context req)
        proxy-id (:proxy-id params)
        query-params (:query-params req)
        requests (requests/get-requests-by-proxy-id
                         {:proxy-id proxy-id
                          :workspace-id (-> context
                                            :user-preferences
                                            :selected_workspace)
                          :status-codes (when-let [status-codes (get query-params "status_codes")]
                                          (when-not (string/blank? status-codes)
                                            (map parse-long
                                                 (string/split
                                                   status-codes
                                                   #","))))
                          :http-methods (when-let [http-methods (get query-params "methods")]
                                          (when-not (string/blank? http-methods)
                                            (string/split
                                              (string/lower-case http-methods)
                                              #",")))
                          :order-by (get query-params "order_by") ;; field
                          :order (get query-params "order") ;; :asc or :desc
                          :customer-id (get query-params "customer_id")
                          :endpoint-id (get query-params "endpoint_id")
                          :limit (or (get query-params "limit") 50)
                          :offset (or (get query-params "offset") 0)})]
    (response {:status "ok"
               :data requests})))

(defn get-one [req params & _]
  (let [context (:context req)
        request-id (:request-id params)
        request (requests/get-request-by-id
                  {:request-id request-id
                   :workspace-id (-> context
                                     :user-preferences
                                     :selected_workspace)})]
    (response {:status "ok"
               :data request})))

(defn query-params-to-map [query-string]
  (when query-string
    (into {}
          (for [param (string/split query-string #"&")]
            (let [[k v] (string/split param #"=")]
              [(keyword k) v])))))

;; Proxy Controller
(defn register-request [req & _]
  (let [body (:body req)
        context (:proxy-context req)
        java-date-created-at (Instant/parse (:created-at body))
        java-date-response-time (Instant/parse (:response-time body))
        _ (t/log! :debug (str "proxy context " context))]
    (t/log! :debug "Registering request")
    (requests/register-request (:workspace-id context)
                               (:id context)
                               {:type (:type body)
                                :url (:url body)
                                :uri (:uri body)
                                :query-params (json/encode (query-params-to-map (:query-params body)))
                                :status-code (:status-code body)
                                :method (:method body)
                                :request-headers (json/generate-string (:request-headers body))
                                :response-headers (json/generate-string (:response-headers body))
                                :response-time java-date-response-time
                                :elapsed-time (.toMillis
                                                (java.time.Duration/between
                                                  java-date-created-at
                                                  java-date-response-time))
                                :created-at java-date-created-at})
    (response {:status "ok"})))
