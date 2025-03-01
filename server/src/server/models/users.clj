(ns server.models.users
  (:require [server.database :as db]
            [taoensso.telemere :as t]
            [pg.honey :as pg-honey]
            [pg.core :as pg]))

(defn create-one
  [{:keys [user workspace-id]}]
  (t/log! :debug "Creating new user")
  (with-open [conn (db/connection)]
    (pg/with-tx [conn]
      (let [new-user (pg-honey/insert-one
                       conn :users {:email (:email user)
                                    :fullname (:fullname user)
                                    :username (:email user)
                                    :password_hash (:password_hash user)}
                       {:returning [:id, :email, :fullname]})
            _ (pg-honey/insert-one conn
                                   :user_preferences
                                   {:user_id (:id new-user)
                                    :selected_workspace workspace-id})

            _ (pg-honey/insert-one conn
                                   :user_workspaces
                                   {:user_id (:id new-user)
                                    :role (:role user)
                                    :workspace_id workspace-id})]
        (:id new-user)))))

(defn update-by-id [user-id workspace-id data]
  (t/log! :debug "Updating user")
  (with-open [conn (db/connection)]
    (pg/with-tx [conn]
      ;; this checks if the user is in the workspace
      (when-let [user-from-workspace (pg-honey/find-first
                                       conn :user_workspaces
                                       {:user_id user-id
                                        :workspace_id workspace-id})]
        (let [updated-user (pg-honey/update
                             conn :users
                             (merge (when-let [fullname (:fullname data)]
                                      {:fullname fullname})
                                    (when-let [username (:username data)]
                                      {:username username}))
                             {:where [:= :id (:id user-from-workspace)]
                              :returning [:id, :email, :fullname]})]
          updated-user)))))

(defn update-role [user-id workspace-id data]
  (t/log! :debug "Updating user")
  (with-open [conn (db/connection)]
    (pg/with-tx [conn]
      (let [updated-role (when (:role data)
                           (pg-honey/update
                             conn :user_workspaces
                             {:role (:role data)}
                             {:where [:and
                                      [:= :user_id user-id]
                                      [:= :workspace_id workspace-id]]}))]
        updated-role))))

(defn delete-by-id [user-id workspace-id]
  (t/log! :debug "Deleting user")
  (with-open [conn (db/connection)]
    (pg/with-tx [conn]
      (when-let [user-from-workspace (pg-honey/find-first
                                       conn :user_workspaces
                                       {:user_id user-id
                                        :workspace_id workspace-id})]
        (let [_ (pg-honey/delete
                  conn :user_workspaces
                  {:where [:= :user_id (:user_id user-from-workspace)]})
              _ (pg-honey/delete
                  conn :user_preferences
                  {:where [:= :user_id (:user_id user-from-workspace)]})
              _ (pg-honey/delete
                  conn :users
                  {:where [:= :id (:user_id user-from-workspace)]})]
          :ok)))))

(defn list-users [workspace-id]
  (t/log! :debug "Getting users")
  (with-open [conn (db/connection)]
    (pg/with-tx [conn]
      (let [workspace-users (pg-honey/find
                        conn :user_workspaces
                        {:workspace_id workspace-id}
                        {:fields [:user_id :role]})
            users (pg-honey/get-by-ids
                   conn :users
                   (mapv :user_id workspace-users)
                   {:fields [:id :email :username :fullname
                             :picture :email_verified]})]
        {:users users
         :workspace-users workspace-users}))))

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
            user-workspace (pg-honey/find-first
                            conn :user_workspaces {:user_id (:id user)})
            workspace (pg-honey/find-first
                        conn :workspaces {:id (:workspace_id user-preferences)})]
        {:user (merge user
                      {:role (:role user-workspace)})
         :user-preferences user-preferences
         :workspace workspace}))))

(defn register-first-workspace-user
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
                                      {:returning [:id, :email, :status
                                                   :fullname, :email_verified]})
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
