(ns server.models.proxies
  (:require
    [taoensso.telemere :as t]
    [pg.honey :as pg-honey]
    [server.database :as db]))

(defn get-proxies [workspace-id]
  (t/log! :debug (str "Getting proxies model for " workspace-id))
  (with-open [conn (db/connection)]
    (pg-honey/find
      conn :proxies
      {:workspace_id workspace-id}
      {:order-by [[:name :asc]]
       :fields [:id :name :description :target_url :status]})))

(defn create-proxy [workspace-id {:keys [name description
                                         target-url proxy-key-hash]}]
  (t/log! :debug (str "Creating proxy model for " workspace-id))
  (with-open [conn (db/connection)]
    (let [new-proxy (pg-honey/insert
                      conn :proxies
                      [{:workspace_id workspace-id
                        :name name
                        :proxy_key_hash proxy-key-hash
                        :status "offline"
                        :description description
                        :target_url target-url}]
                      {:returning [:id :name :description :target_url]})]
      new-proxy)))

(defn get-proxy-by-id [proxy-id]
  (t/log! :debug (str "Getting proxy model by id " proxy-id))
  (with-open [conn (db/connection)]
    (pg-honey/get-by-id
      conn :proxies
      proxy-id)))

(defn update-proxy-status [workspace-id proxy-id status]
  (t/log! :debug (str "Updating proxy status for " proxy-id))
  (with-open [conn (db/connection)]
    (pg-honey/update
      conn :proxies
      {:status status}
      {:where [:and
               [:= :id proxy-id]
               [:= :workspace_id workspace-id]]
       :returning [:target_url :description]})))

(defn update-proxy-by-id [workspace-id proxy-id [{:keys [name description
                                                   target-url]}]]
  (t/log! :debug (str "Updating proxy model for " proxy-id))
  (with-open [conn (db/connection)]
    (pg-honey/update
      conn :proxies
      {:name name
       :description description
       :target_url target-url}
      {:where [:and
               [:= :id proxy-id]
               [:= :workspace_id workspace-id]]
       :returning [:*]})))

(defn delete-proxy [proxy-id workspace-id]
  (t/log! :debug (str "Deleting proxy model for " proxy-id))
  (with-open [conn (db/connection)]
    (pg-honey/delete
      conn :proxies
      {:id proxy-id
       :workspace_id workspace-id})))

(defn set-proxy-alive [proxy-id workspace-id]
  (t/log! :debug (str "Setting proxy alive for " proxy-id))
  (update-proxy-status proxy-id "online"))
