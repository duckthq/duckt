(ns server.api.events
  (:require
    [cheshire.core :as json :refer [generate-string]]
    [clojure.string :as string]
    [ring.util.response :refer [response]]
    [taoensso.telemere :as t]
    [server.models.events :as events]))
(import [java.time Instant])

(defn get-timeframe-events-by-proxy [req params & _]
  (t/log! :debug "Getting events by timeframe")
  (let [context (:context req)
        query-params (:query-params req)
        start-time (java.time.Instant/parse (get query-params "start-time"))
        end-time (java.time.Instant/parse (get query-params "end-time"))
        status-code-group (when-let [code (get query-params "status-code-group")]
                             (when-not (string/blank? code)
                               (parse-long code)))
        events (events/get-timeframe-events-by-proxy
                         {:workspace-id (-> context
                                            :user-preferences
                                            :selected_workspace)
                          :proxy-id (:proxy-id params)
                          :status-code-group status-code-group
                          :start-time start-time
                          :end-time end-time})]
    (response {:status "ok"
               :data {:total (apply + (map :total events))
                      :status-code-group (if status-code-group
                                           (str status-code-group "xx")
                                           "all")
                      :list events}})))

(defn get-timeframe-events [req & _]
  (t/log! :debug "Getting events by timeframe")
  (let [context (:context req)
        query-params (:query-params req)
        start-time (java.time.Instant/parse (get query-params "start-time"))
        end-time (java.time.Instant/parse (get query-params "end-time"))
        status-code-group (when-let [code (get query-params "status-code-group")]
                             (when-not (string/blank? code)
                               (parse-long code)))
        events (events/get-timeframe-events
                         {:workspace-id (-> context
                                            :user-preferences
                                            :selected_workspace)
                          :status-code-group status-code-group
                          :start-time start-time
                          :end-time end-time})]
    (response {:status "ok"
               :data {:total (apply + (map :total events))
                      :status-code-group (if status-code-group
                                           (str status-code-group "xx")
                                           "all")
                      :list events}})))

(defn get-events-by-proxy-id [req params & _]
  (let [context (:context req)
        proxy-id (:proxy-id params)
        query-params (:query-params req)
        events (events/get-events-by-proxy-id
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
                          :start-date (when-let [start-date (get query-params "start_date")]
                                        (Instant/parse start-date))
                          :end-date (when-let [end-date (get query-params "end_date")]
                                      (Instant/parse end-date))
                          :customer-ids (when-let [customer-ids (get query-params "customer_ids")]
                                          (when-not (string/blank? customer-ids)
                                            (string/split customer-ids #",")))
                          :customers-subs (when-let [customers-subs (get query-params "customers_subs")]
                                            (when-not (string/blank? customers-subs)
                                              (string/split customers-subs #",")))
                          :endpoints-ids (when-let [endpoints-ids (get query-params "endpoints_ids")]
                                           (when-not (string/blank? endpoints-ids)
                                             (string/split endpoints-ids #",")))
                          :order-by (get query-params "order_by") ;; field
                          :order (get query-params "order") ;; :asc or :desc
                          :customer-id (get query-params "customer_id")
                          :endpoint-id (get query-params "endpoint_id")
                          :limit (if-let [limit (get query-params "limit")]
                                   (parse-long limit)
                                   100)
                          :offset (or (get query-params "offset") 0)})]
    (response {:status "ok"
               :data events})))

(defn get-one [req params & _]
  (let [context (:context req)
        event-id (:event-id params)
        event (events/get-event-by-id
                  {:event-id event-id
                   :workspace-id (-> context
                                     :user-preferences
                                     :selected_workspace)})]
    (response {:status "ok"
               :data event})))

(defn query-params-to-map [query-string]
  (when query-string
    (into {}
          (for [param (string/split query-string #"&")]
            (let [[k v] (string/split param #"=")]
              [(keyword k) v])))))

;; Proxy Controller
(defn register-event [req & _]
  (let [body (:body req)
        context (:proxy-context req)
        java-date-created-at (Instant/parse (:created-at body))
        java-date-response-time (Instant/parse (:response-time body))
        event-headers (json/generate-string (:event-headers body))
        duckt-user-sub (-> body :event-headers :duckt-user-sub)
        response-headers (json/generate-string (:response-headers body))]
    (t/log! :debug "Registering event")
    (events/register-event (:workspace-id context)
                               (:id context)
                               {:type (:type body)
                                :url (:url body)
                                :uri (:uri body)
                                :query-params (json/encode (query-params-to-map (:query-params body)))
                                :status-code (:status-code body)
                                :method (:method body)
                                :event-headers event-headers
                                :duckt-user-sub duckt-user-sub
                                :response-headers response-headers
                                :response-time java-date-response-time
                                :elapsed-time (.toMillis
                                                (java.time.Duration/between
                                                  java-date-created-at
                                                  java-date-response-time))
                                :created-at java-date-created-at})
    (response {:status "ok"})))
