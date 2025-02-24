(ns server.api.users
  (:require
    [ring.util.response :refer [response]]
    [crypto.random :as random]
    [buddy.hashers :as buddy]
    [taoensso.telemere :as t]
    [server.models.users :as users]))

(defn get-all [req & _]
  (t/log! :debug "Getting users")
  (let [context (:context req)
        user-workspace (-> context :user-preferences :selected_workspace)
        users (users/list-users user-workspace)
        res (map (fn [user]
                   (let [u (filter #(= (:id %) (:user_id user)) (:users users))]
                     (merge {:role (:role user)}
                            (first u))))
                 (:workspace-users users))]
    (response {:status "ok"
               :data res})))

(defn create-one [req & _]
  (t/log! :debug "Creating user")
  (let [context (:context req)
        body (:body req)
        workspace-id (-> context :user-preferences :selected_workspace)
        new-password (random/base32 16)
        new-user (users/create-one {:user {:email (:email body)
                                           :fullname (:fullname body)
                                           :password_hash (buddy/derive new-password)
                                           :role (:role body)}
                                    :workspace-id workspace-id})]
    (response {:status "ok"
               :data {:id new-user
                      :email (:email body)
                      :password new-password}})))


(defn update-role [req params & _]
  (t/log! :debug "Updating user user")
  (let [context (:context req)
        body (:body req)
        user-id (:user-id params)
        workspace-id (-> context :user-preferences :selected_workspace)
        updated-user (users/update-role
                       user-id
                       workspace-id
                       (when-let [role (when
                                         (contains? #{"admin" "member" "owner"}
                                                    (:role body))
                                         (:role body))]
                         {:role role}))]
    (response {:status "ok"
               :data updated-user})))

(defn delete-one [req params & _]
  (t/log! :debug "Deleting user")
  (let [context (:context req)
        user-id (:user-id params)
        workspace-id (-> context :user-preferences :selected_workspace)]
    (users/delete-by-id user-id workspace-id)
    (response {:status "ok"})))

(defn update-one [req params & _]
  (t/log! :debug "Updating user")
  (let [context (:context req)
        body (:body req)
        user-id (:user-id params)
        workspace-id (-> context :user-preferences :selected_workspace)
        updated-user (users/update-by-id
                       user-id
                       workspace-id
                       (merge
                         (when-let [fullname (:fullname body)]
                           {:fullname fullname})
                         (when-let [role (when
                                           (contains? #{"admin" "member" "owner"}
                                                      (:role body))
                                           (:role body))]
                           {:role role})))]
    (response {:status "ok"
               :data updated-user})))
