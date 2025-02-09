(ns server.models.workspaces
  (:require
    [taoensso.telemere :as t]
    [server.database :as db]
    [pg.honey :as pg-honey]
    [pg.core :as pg]))

(defn get-workspaces
  "Finds all workspaces for the user in the context"
  [user-id]
  (t/log! :debug (str "Getting workspaces model for " user-id))
  (with-open [conn (db/connection)]
    (pg/with-tx [conn]
      (let [user-workspaces (pg-honey/find
                              conn :user_workspaces {:user_id user-id})]
        (first (mapv
                 #(pg-honey/find
                    conn :workspaces
                    {:id (:workspace_id %)}
                    {:order-by [[:name :asc]]})
                 user-workspaces))))))

(defn create-workspace
  "Creates a new workspace for the user in the context"
  [user-id [{:keys [name description]}]]
  (t/log! :debug (str "Creating workspace model for " user-id))
  (with-open [conn (db/connection)]
    (pg/with-tx [conn]
      (let [workspace (pg-honey/insert-one
                        conn :workspaces
                        {:name name :description description}
                        {:returning [:id :name :description]})
            _ (pg-honey/insert-one
                conn :user_workspaces
                {:user_id user-id
                 :workspace_id (:id workspace)
                 :role :admin})]
        workspace))))
