(ns server.models.users
  (:require [server.database :as db]
            [taoensso.telemere :as t]
            [pg.honey :as pg-honey]
            [pg.core :as pg]))

(defn create [user]
  (t/log! :debug "Creating user")
  (pg-honey/insert-one @db/conn :users user {:returning [:*]}))

(defn get-one-by-email [email]
  (t/log! :debug "Getting user by email")
  (with-open [connection (db/connection)]
    (pg-honey/find-first connection :users {:email email})))

(defn build-user-context
  "Gets the user email and builds their context
  with tables: users, user_preferences and workspaces"
  [email]
  (t/log! :debug "Building user context")
  (with-open [conn (db/connection)]
    (pg/with-tx [conn]
      (let [user (pg-honey/find-first
                   conn :users {:email email})
            user-preferences (pg-honey/find-first
                               conn :user_preferences {:user_id (:id user)})
            workspace (pg-honey/find-first
                        conn :workspaces {:id (:workspace_id user-preferences)})]
        {:user user
         :user-preferences user-preferences
         :workspace workspace}))))

(defn register-new-user
  "This sets a new workspace and register the new user to it"
  [user]
  (t/log! :debug "Registering new user")
  (with-open [conn (db/connection)]
    (pg/with-tx [conn]
      (let [new-workspace {:name (str (:email user) "'s workspace")
                           :description "Default workspace for new users"}
            workspace (pg-honey/insert-one conn
                                           :workspaces
                                           new-workspace
                                           {:returning [:id,
                                                        :name,
                                                        :description]})
            user (pg-honey/insert-one conn
                                      :users
                                      user
                                      {:returning [:id, :email, :username,
                                                   :given_name, :family_name
                                                   :picture, :email_verified]})
            _ (pg-honey/insert-one conn
                                   :user_preferences
                                   {:user_id (:id user)
                                    :selected_workspace (:id workspace)})

            _ (pg-honey/insert-one conn
                                   :user_workspaces
                                   {:user_id (:id user)
                                    :role "owner"
                                    :workspace_id (:id workspace)})]
        {:user user
         :workspace workspace}))))
