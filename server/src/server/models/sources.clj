(ns server.models.sources
  (:require
    [taoensso.telemere :as t]
    [pg.honey :as pg-honey]
    [server.database :as db]))

(defn get-sources [workspace-id]
  (t/log! :debug (str "Getting sources model for " workspace-id))
  (with-open [conn (db/connection)]
    (pg-honey/find
      conn :sources
      {:workspace_id workspace-id}
      {:order-by [[:name :asc]]
       :fields [:id :name :description :created_at :updated_at :workspace_id]})))

(defn create-source [workspace-id {:keys [name description source-key-hash]}]
  (t/log! :debug (str "Creating source model for " workspace-id))
  (with-open [conn (db/connection)]
    (let [new-source (pg-honey/insert
                      conn :sources
                      [{:workspace_id workspace-id
                        :name name
                        :key_hash source-key-hash
                        :description description}]
                      {:returning [:id :name :description]})]
      new-source)))

(defn get-source [workspace-id source-id]
  (t/log! :debug (str "Getting source model by id " source-id))
  (with-open [conn (db/connection)]
    (pg-honey/find-first
      conn :sources
      {:workspace_id workspace-id
       :id source-id}
      {:fields [:id :name :description
                :created_at :updated_at]})))

(defn update-source-status [workspace-id source-id status]
  (t/log! :debug (str "Updating source status for " source-id))
  (with-open [conn (db/connection)]
    (pg-honey/update
      conn :sources
      {:status status}
      {:where [:and
               [:= :id source-id]
               [:= :workspace_id workspace-id]]
       :returning [:target_url :description
                   :response_headers_config
                   :request_headers_config]})))

(defn update-source-by-id
  [workspace-id source-id {:keys [name description target-url host-url
                                 response-headers-config request-headers-config]}]
  (t/log! :debug (str "Updating source model for " source-id))
  (with-open [conn (db/connection)]
    (pg-honey/update
      conn :sources
      (merge
        (when response-headers-config
          {:response_headers_config response-headers-config})
        (when request-headers-config
          {:request_headers_config request-headers-config})
        (when name {:name name})
        (when description {:description description})
        (when target-url {:target_url target-url})
        (when host-url {:host_url host-url}))
      {:where [:and
               [:= :id source-id]
               [:= :workspace_id workspace-id]]
       :returning [:id]})))

(defn generate-new-source-key [source-id workspace-id new-key]
  (t/log! :debug (str "Generating new key for source " source-id))
  (with-open [conn (db/connection)]
    (pg-honey/update
      conn :sources
      {:source_key_hash new-key}
      {:where [:and
               [:= :id source-id]
               [:= :workspace_id workspace-id]]
       :returning [:id]})))

(defn delete-source [source-id workspace-id]
  (t/log! :debug (str "Deleting source model for " source-id))
  (println :model source-id workspace-id)
  (with-open [conn (db/connection)]
    (pg-honey/delete
      conn :sources
      {:where [:and
               [:= :id source-id]
               [:= :workspace_id workspace-id]]
       :returning [:id]})))

;; Used for source authentication
(defn get-source-by-id [source-id]
  (t/log! :debug (str "Getting source model by id " source-id))
  (with-open [conn (db/connection)]
    (pg-honey/get-by-id
      conn :sources
      source-id)))
