(ns server.controllers.workspaces
  (:require
    [ring.util.response :refer [response]]
    [taoensso.telemere :as t]
    [server.models.workspaces :as workspaces]))

(defn get-workspaces [req & _]
  (t/log! :debug "Getting workspaces")
  (let [context (:context req)
        user (:user context)]
    (response {:status "ok"
               :data (workspaces/get-workspaces (:id user))})))

(defn create-workspace [req & _]
  (t/log! :debug "Creating workspace")
  (let [context (:context req)
        user (:user context)
        body (:body req)]
    (response {:status "ok"
               :data (workspaces/create-workspace
                       (:id user)
                       {:name (:name body)
                        :description (:description body)})})))
