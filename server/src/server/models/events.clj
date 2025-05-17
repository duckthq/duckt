(ns server.models.events
  (:require
    [taoensso.telemere :as t]
    [server.database :as db]
    [pg.honey :as pg-honey]
    [pg.core :as pg]))

(defn register-event [workspace-id
                      proxy-id
                      {:keys [type url uri query-params status-code
                              method event-headers response-headers
                              response-time created-at elapsed-time
                              duckt-user-sub]}]
  ; TODO: maybe create the event ID in the beginning
  ; so we can improve tracing in the logs?
  (t/log! :debug "Registering event")
  (with-open [conn (db/connection)]
    (pg/with-tx [conn]
      (let [event {:type type
                   :url url
                   :uri uri
                   :query_params query-params
                   :proxy-id proxy-id
                   :status_code status-code
                   :event_headers event-headers
                   :response_headers response-headers
                   :response_time response-time
                   :elapsed_time elapsed-time
                   :created_at created-at
                   :method method
                   :workspace_id workspace-id}
            customer (when duckt-user-sub
                       (pg-honey/insert-one
                         conn :customers
                         {:sub duckt-user-sub
                          :workspace_id workspace-id}
                         {:on-conflict [:sub :workspace_id]
                          :do-update-set {:last_seen_at created-at
                                          :hit_count [:+ :customers.hit_count 1]}
                          :returning [:id :sub]}))
            save-endpoint (fn []
                            (let [local-date (java.time.LocalDate/now)]
                              (pg-honey/insert-one conn :endpoints
                                                   {:path uri
                                                    :proxy_id proxy-id
                                                    :method method
                                                    :workspace_id workspace-id}
                                                   {:on-conflict [:path :proxy_id :method]
                                                    :do-update-set {:hit_count [:+ :endpoints.hit_count 1]
                                                                    :last_used_at local-date}
                                                    :returning [:id]})))
            endpoint (save-endpoint)]

        (pg-honey/insert-one conn :events
                             (merge event
                                    {:customer_id (:id customer)
                                     :customer_sub (:sub customer)}
                                    {:endpoint_id (:id endpoint)}))))))

;; Get events by workspace_id, type, with limit and offsite
(defn get-events [{:keys [workspace-id type limit offset]}]
  (t/log! :debug "Getting events")
  (with-open [conn (db/connection)]
    (pg/with-tx [conn]
      (let [query {;:workspace_id workspace-id
                   :type "proxy"}
            options {:limit limit
                     :order-by [[:created_at :asc]]
                     :offset offset
                     :fields [:id, :type, :url, :uri, :elapsed_time,
                              :status_code, :endpoint_id,
                              :event_headers, :response_headers,
                              :method, :response_time, :created_at]}]
        (pg-honey/find conn :events query options)))))

(defn get-timeframe-events
  [{:keys [workspace-id status-code-group start-time end-time]}]
  (t/log! :debug (str "Getting events by proxy id"))
  (with-open [conn (db/connection)]
    (pg/with-tx [conn]
      (let [trunc-unit "hour"
            interval "1 hour"
            params {:params [workspace-id start-time end-time trunc-unit]}
            events (pg/execute
                     conn
                     (str "WITH time_series AS (
                                                SELECT generate_series(
                                                                       date_trunc($4, $2::timestamp),
                                                                       date_trunc($4, $3::timestamp),
                                                                       '" interval "'::interval
                                                                       ) as time_bucket
                                                )
                          SELECT
                          time_series.time_bucket,
                          COALESCE(count.total, 0) as total
                          FROM time_series
                          LEFT JOIN (
                                     SELECT
                                     date_trunc($4, created_at AT TIME ZONE 'UTC') as time_bucket,
                                     count(*) as total
                                     FROM events
                                     WHERE created_at >= $2
                                     AND created_at <= $3"
                                     (when status-code-group
                                       (str " AND status_code >= " status-code-group "00"
                                            " AND status_code <= " status-code-group "99"))
                                     " AND workspace_id = $1
                                     GROUP BY 1
                                     ) count ON time_series.time_bucket = count.time_bucket
                          ORDER BY time_series.time_bucket;")
                     params)]
        events))))

(defn get-timeframe-events-by-proxy
  [{:keys [proxy-id workspace-id status-code-group start-time end-time]}]
  (t/log! :debug (str "Getting events by proxy id"))
  (with-open [conn (db/connection)]
    (pg/with-tx [conn]
      (let [trunc-unit "hour"
            interval "1 hour"
            params {:params [workspace-id proxy-id start-time end-time trunc-unit]}
            events (pg/execute
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
                                     FROM events
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
        events))))

(defn get-events-by-proxy-id
  [{:keys [proxy-id workspace-id limit offset
           order order-by customer-ids customers-subs
           start-date end-date
           status-codes http-methods endpoints-ids]}]
  (t/log! :debug "Getting events by proxy id")
  (with-open [conn (db/connection)]
    (pg/with-tx [conn]
      (let [query {:select [:id :type :url :uri :elapsed_time
                            :status_code :endpoint_id :customer_id
                            :customer_sub :workspace_id
                            :method :response_time :created_at]
                   :from :events
                   :where [:and
                           (when endpoints-ids
                             [:in :endpoint_id endpoints-ids])
                           (when customer-ids
                             [:in :customer_id customer-ids])
                           (when customers-subs
                             [:in :customer_sub customers-subs])
                           (when start-date
                             [:>= :created_at start-date])
                           (when end-date
                             [:<= :created_at end-date])
                           (when http-methods
                             [:in :method http-methods])
                           (when status-codes
                             [:in :status_code status-codes])
                           [:= :proxy_id proxy-id]
                           [:= :workspace_id workspace-id]]
                   :order-by [[(or order-by :created_at)
                               (or order :desc)]]
                   :limit limit
                   :offset offset}
            events (pg-honey/execute conn query)]
        {:list events}))))

(defn get-event-by-id [{:keys [event-id workspace-id]}]
  (t/log! :debug "Getting event by id")
  (with-open [conn (db/connection)]
    (let [query {:id event-id
                 :workspace_id workspace-id}
          options {:fields [:id, :type, :url, :uri, :elapsed_time,
                            :event_headers, :response_headers,
                            :status_code, :endpoint_id, :customer_id
                            :query_params :customer_sub
                            :method, :response_time, :created_at]}
          event (pg-honey/find-first conn :events query options)]
      event)))
