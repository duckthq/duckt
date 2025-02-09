(ns server.models.endpoints
  (:require
    [taoensso.telemere :as t]
    [server.database :as db]
    [pg.honey :as pg-honey]
    [pg.core :as pg]))

(defn get-endpoints []
  (t/log! :debug "Getting endpoints")
  (with-open [conn (db/connection)]
  (let [query {}
        options {:order-by [[:path :asc]]
                 :fields [:id :path :hit_count :method
                          :host_id :workspace_id
                          :last_used_at :created_at]}]
    (pg-honey/find conn :endpoints query options))))

(defn get-endpoint-by-id [endpoint-id]
  (t/log! :debug "Getting endpoint by id")
  (with-open [conn (db/connection)]
    (let [query {:id endpoint-id}
          options {:fields [:id :path :hit_count :method
                            :host_id :workspace_id
                            :last_used_at :created_at]}]
      (pg-honey/find-first conn :endpoints query options))))
