(ns server.models.requests
  (:require
    [clojure.string :as string]
    [taoensso.telemere :as t]
    [buddy.core.codecs.base64 :as base64]
    [cheshire.core :as json]
    [server.database :as db]
    [pg.honey :as pg-honey]
    [pg.core :as pg]))

(defn register-request [workspace-id
                        proxy-id
                        {:keys [type url uri query-params status-code
                                method request-headers response-headers
                                response-time created-at elapsed-time]}]
  ; TODO: maybe create the request ID in the beginning
  ; so we can improve tracing in the logs?
  (t/log! :debug "Registering request")
  (let [authorization-token (first (get request-headers "Authorization"))
        ;; TODO move this logic to a middleware and refactor this
        claims (when authorization-token
                 (-> authorization-token
                     (string/split #"\.")
                     (second)
                     (base64/decode)
                     (String.)
                     (json/parse-string true)))]
    (with-open [conn (db/connection)]
      (pg/with-tx [conn]
        (let [request {:type type
                       :url url
                       :uri uri
                       :query_params query-params
                       :proxy-id proxy-id
                       :status_code status-code
                       :request_headers request-headers
                       :response_headers response-headers
                       :response_time response-time
                       :elapsed_time elapsed-time
                       :created_at created-at
                       :method method
                       :workspace_id workspace-id}
              ;identifiable-request? (not (nil? (:sub claims)))
              ;customer? (when identifiable-request?
              ;            (pg-honey/find-first conn :host_users {:sub (:sub claims)}))
              endpoint? (pg-honey/find-first conn :endpoints {:path uri
                                                              :proxy_id proxy-id
                                                              :method method} {:fields [:id]})
              save-endpoint (fn []
                              (let [local-date (java.time.LocalDate/now)]
                                ;; The system is limited to running as a single instance
                                ;; so we can safely assume that this count up is safe
                                (if (> (count endpoint?) 0)
                                  (first (pg-honey/update conn
                                                          :endpoints
                                                          {:hit_count [:+ :hit_count 1]
                                                           :last_used_at local-date}
                                                          {:where [:= :id (:id endpoint?)]}))
                                  (pg-honey/insert-one conn :endpoints
                                                       {:path uri
                                                        :proxy_id proxy-id
                                                        :method method
                                                        :workspace_id workspace-id}))))
              ;; TODO refactor this to an actual upsert
              upsert-customer (fn [claimed-user customer]
                                (if (> (count customer) 0)
                                  (first (pg-honey/update conn
                                                          :customers
                                                          {:email (:email claimed-user)}
                                                          {:where [:= :id (:id customer)]}))
                                  (pg-honey/insert-one conn :customers
                                                       {:sub (:sub claimed-user)
                                                        :email (:email claimed-user)
                                                        :workspace_id workspace-id})))
              endpoint (save-endpoint)
              ;customer (when identifiable-request?
              ;           (upsert-customer claims customer?))
              ]
          (pg-honey/insert-one conn :requests
                               (merge request
                                      {:customer_id nil}
                                      {:endpoint_id (:id endpoint)})))))))

;; Get requests by workspace_id, type, with limit and offsite
(defn get-requests [{:keys [workspace-id type limit offset]}]
  (t/log! :debug "Getting requests")
  (with-open [conn (db/connection)]
    (pg/with-tx [conn]
      (let [query {;:workspace_id workspace-id
                   :type "proxy"}
            options {:limit limit
                     :order-by [[:created_at :asc]]
                     :offset offset
                     :fields [:id, :type, :url, :uri, :elapsed_time,
                              :status_code, :endpoint_id,
                              :request_headers, :response_headers,
                              :method, :response_time, :created_at]}]
        (pg-honey/find conn :requests query options)))))

(defn get-timeframe-requests-by-proxy
  [{:keys [proxy-id workspace-id status-code-group start-time end-time]}]
  (t/log! :debug (str "Getting requests by proxy id"))
  (with-open [conn (db/connection)]
    (pg/with-tx [conn]
      (let [trunc-unit "hour"
            interval "1 hour"
            params {:params [workspace-id proxy-id start-time end-time trunc-unit]}
            requests (pg/execute
                       conn
                       (str "WITH time_series AS (
                         SELECT generate_series(
                           date_trunc($5, $3::timestamp),
                           date_trunc($5, $4::timestamp),
                           '" interval "'::interval
                         ) as time_bucket
                       )
                       SELECT
                         time_series.time_bucket,
                         COALESCE(count.total, 0) as total
                       FROM time_series
                       LEFT JOIN (
                         SELECT
                           date_trunc($5, created_at AT TIME ZONE 'UTC') as time_bucket,
                           count(*) as total
                         FROM requests
                         WHERE created_at >= $3
                         AND created_at <= $4"
                         (when status-code-group
                           (str " AND status_code >= " status-code-group "00"
                                " AND status_code <= " status-code-group "99"))
                         " AND proxy_id = $2
                         AND workspace_id = $1
                         GROUP BY 1
                       ) count ON time_series.time_bucket = count.time_bucket
                       ORDER BY time_series.time_bucket;")
                       params)]
        requests))))

(defn get-requests-by-proxy-id
  [{:keys [proxy-id workspace-id limit offset
           order order-by customer-id
           start-date end-date
           status-codes http-methods endpoint-id]}]
  (t/log! :debug "Getting requests by proxy id")
  (with-open [conn (db/connection)]
    (pg/with-tx [conn]
      (let [query {:select [:id :type :url :uri :elapsed_time
                            :status_code :endpoint_id :customer_id
                            :method :response_time :created_at]
                   :from :requests
                   :where [:and
                           (when http-methods
                             [:in :method http-methods])
                           (when status-codes
                             [:in :status_code status-codes])
                           [:= :proxy_id :proxy-id]
                           [:= :workspace_id :workspace-id]]
                   :order-by [[(or order-by :created_at)
                               (or order :desc)]]
                   :limit limit
                   :offset offset}
            params {:honey {:params {:proxy-id proxy-id
                                     :workspace-id workspace-id}}}
            requests (pg-honey/execute conn query params)]
        {:list requests}))))

(defn get-request-by-id [{:keys [request-id workspace-id]}]
  (t/log! :debug "Getting request by id")
  (with-open [conn (db/connection)]
    (let [query {:id request-id
                 :workspace_id workspace-id}
          options {:fields [:id, :type, :url, :uri, :elapsed_time,
                            :request_headers, :response_headers,
                            :status_code, :endpoint_id, :customer_id
                            :method, :response_time, :created_at]}
          request (pg-honey/find-first conn :requests query options)]
      request)))

;(defn count-requests-by-endpoint-id [{:keys [endpoint-id]}]
;  (t/log! :debug "Counting requests by endpoint id")
;  (with-open [conn (db/connection)]
;    (pg/execute
;      conn
;      "select count(id) from requests where endpoint_id = $1"
;      {:params [endpoint-id]})))
