(ns server.api.customers
  (:require
    [clojure.string :as string]
    [cheshire.core :refer [generate-string]]
    [taoensso.telemere :as t]
    [server.models.customers :as customers-model]))

(defn list-customers [req _ _]
  (t/log! :debug "Listing customers")
  (let [context (:context req)
        workspace-id (-> context :user-preferences :selected_workspace)
        query-params (:query-params req)
        customers (customers-model/list-customers
                    {:workspace-id workspace-id
                     :customer-subs (when-let [subs (get query-params "customer_subs")]
                                      (when-not (string/blank? subs)
                                        (map parse-long
                                             (string/split subs #","))))
                     :customer-ids (when-let [ids (get query-params "customer_ids")]
                                     (when-not (string/blank? ids)
                                       (map parse-long
                                            (string/split ids #","))))
                     :limit (if-let [limit (get query-params "limit")]
                              (parse-long limit)
                              50)
                     :offset (or (get query-params "offset") 0)
                     :order-by (or (get query-params "order_by") :joined_at)
                     :order (or (get query-params "order") :desc)})]
    (generate-string
      {:status "ok"
       :data customers})))
