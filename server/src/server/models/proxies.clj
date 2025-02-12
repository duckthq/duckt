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

(defn create-proxy [workspace-id {:keys [name description host-url
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
                        :host_url host-url
                        :target_url target-url}]
                      {:returning [:id :name :description :target_url]})]
      new-proxy)))

(defn get-proxy [workspace-id proxy-id]
  (t/log! :debug (str "Getting proxy model by id " proxy-id))
  (with-open [conn (db/connection)]
    (pg-honey/find-first
      conn :proxies
      {:workspace_id workspace-id
       :id proxy-id}
      {:fields [:id :name :description
                :target_url :host_url :status]})))



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

(defn update-proxy-by-id
  [workspace-id proxy-id {:keys [name description
                                  target-url host-url]}]
  (t/log! :debug (str "Updating proxy model for " proxy-id))
  (with-open [conn (db/connection)]
    (pg-honey/update
      conn :proxies
      {:name name
       :description description
       :target_url target-url
       :host_url host-url}
      {:where [:and
               [:= :id proxy-id]
               [:= :workspace_id workspace-id]]
       :returning [:id]})))

(defn generate-new-proxy-key [proxy-id workspace-id new-key]
  (t/log! :debug (str "Generating new key for proxy " proxy-id))
  (with-open [conn (db/connection)]
    (pg-honey/update
      conn :proxies
      {:proxy_key_hash new-key}
      {:where [:and
               [:= :id proxy-id]
               [:= :workspace_id workspace-id]]
       :returning [:id]})))

(defn delete-proxy [proxy-id workspace-id]
  (t/log! :debug (str "Deleting proxy model for " proxy-id))
  (println :model proxy-id workspace-id)
  (with-open [conn (db/connection)]
    (pg-honey/delete
      conn :proxies
      {:where [:and
               [:= :id proxy-id]
               [:= :workspace_id workspace-id]]
       :returning [:id]})))

;; Used for proxy authentication
(defn get-proxy-by-id [proxy-id]
  (t/log! :debug (str "Getting proxy model by id " proxy-id))
  (with-open [conn (db/connection)]
    (pg-honey/get-by-id
      conn :proxies
      proxy-id)))
