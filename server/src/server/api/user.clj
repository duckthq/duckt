(ns server.api.user
  (:require
    [ring.util.response :refer [response]]
    [taoensso.telemere :as t]))

(defn user-info [req & _]
  (t/log! :debug "Getting user info")
  (let [context (:context req)
        user (:user context)
        user-preferences (:user-preferences context)]
    (response {:data {:id (:id user)
                      :email (:email user)
                      :username (:username user)
                      :profile (:profile user)
                      :fullname (:fullname user)
                      :family_name (:family_name user)
                      :given_name (:given_name user)
                      :user_preferences user-preferences
                      :workspace (:workspace context)}})))
