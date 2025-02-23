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

(defn update-one [req params & _]
  (t/log! :debug "Updating user")
  (let [context (:context req)
        body (:body req)
        user-id (:user_id params)
        workspace-id (-> context :user-preferences :selected_workspace)
        updated-user (users/update-one {:user-id user-id
                                        :workspace-id workspace-id
                                        :user {:email (:email body)
                                               :role (:role body)}})]
    (response {:status "ok"
               :data updated-user})))
