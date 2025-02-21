(ns server.models.customers
  (:require
    [pg.honey :as pg-honey]
    [pg.core :as pg]
    [taoensso.telemere :as t]
    [server.database :as db]))

(defn list-customers
  [{:keys [workspace-id limit offset order order-by
           customer-ids customer-subs]}]
  (t/log! :debug "Getting requests by proxy id")
  (with-open [conn (db/connection)]
    (pg/with-tx [conn]
      (let [query {:select [:*]
                   :from :customers
                   :where [:and
                           (when customer-ids
                             [:in :id customer-ids])
                           (when customer-subs
                             [:in :sub customer-subs])
                           [:= :workspace_id workspace-id]]
                   :order-by [[(or order-by :joined_at)
                               (or order :desc)]]
                   :limit limit
                   :offset offset}
            customers (pg-honey/execute conn query)]
        {:list customers}))))
