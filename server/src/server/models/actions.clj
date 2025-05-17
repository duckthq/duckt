(ns server.models.actions
  (:require
    [taoensso.telemere :as t]
    [pg.honey :as pg-honey]
    [server.database :as db]))

(defn get-all [workspace-id]
  (t/log! :debug (str "Getting actions model"))
  (with-open [conn (db/connection)]
    (pg-honey/find
      conn :actions
      {:workspace_id workspace-id}
      {:order-by [[:name :asc]]
       :fields [:id :name :type :config :description :created_at :updated_at :workspace_id]})))

(defn get-one [workspace-id action-id]
  (t/log! :debug (str "Getting action model by id " action-id))
  (with-open [conn (db/connection)]
    (pg-honey/find-first
      conn :actions
      {:workspace_id workspace-id
       :id action-id}
      {:fields [:id :name :type :config :description
                :created_at :updated_at]})))

(defn upsert-one [workspace-id action-id {:keys [name description type config]}]
  (t/log! :debug (str "upserting action in the db"))
  (with-open [conn (db/connection)]
    (let [new-source (pg-honey/insert-one
                      conn :actions
                      {:workspace_id workspace-id
                        :id action-id
                        :name name
                        :type type
                        :config config
                        :description description}
                      {:on-conflict [:id]
                       :do-update-set [:name :type
                                       :config :description]
                       :returning [:id :name :description]})]
      new-source)))

(defn delete-one [workspace-id action-id]
  (t/log! :debug (str "deleting action in the db"))
  (with-open [conn (db/connection)]
    (pg-honey/delete
      conn :actions
      {:workspace_id workspace-id
       :id action-id})))

(defn insert-one [workspace-id {:keys [name description type config]}]
  (t/log! :debug (str "inserting action in the db"))
  (with-open [conn (db/connection)]
    (let [new-source (pg-honey/insert
                      conn :actions
                      [{:workspace_id workspace-id
                        :name name
                        :type type
                        :config config
                        :description description}]
                      {:returning [:id :name :description]})]
      new-source)))
