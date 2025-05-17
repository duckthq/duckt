(ns server.api.actions
  (:require
    [ring.util.response :refer [response]]
    [taoensso.telemere :as t]
    [server.models.actions :as actions]))

(defn get-actions [req & _]
  (t/log! :debug "Getting all actions")
  (let [context (:context req)
        selected-workspace (-> context
                               :user-preferences
                               :selected_workspace)
        actions (actions/get-all selected-workspace)]
    (response {:data actions})))

(defn get-action-by-id [req params & _]
  (t/log! :debug "Getting action by id")
  (let [context (:context req)
        action-id (:action-id params)
        selected-workspace (-> context
                               :user-preferences
                               :selected_workspace)]
    (response {:data (actions/get-one selected-workspace action-id)})))

(defn update-action-by-id [req params & _]
  (t/log! :debug "Updating action")
  (let [context (:context req)
        body (:body req)
        action-id (:action-id params)
        selected-workspace (-> context
                               :user-preferences
                               :selected_workspace)
        updated-action (actions/upsert-one
                         selected-workspace
                         action-id
                         {:name (:name body)
                          :description (:description body)
                          :type (:type body)
                          :config (:config body)})]
    (response {:data updated-action})))

(defn delete-action-by-id [req params & _]
  (t/log! :debug "Deleting action")
  (let [context (:context req)
        action-id (:action-id params)
        selected-workspace (-> context
                               :user-preferences
                               :selected_workspace)]
    (actions/delete-one selected-workspace action-id)
    (response {:status "ok"})))

(defn create [req & _]
  (t/log! :debug "Creating action")
  (let [context (:context req)
        body (:body req)
        selected-workspace (-> context
                               :user-preferences
                               :selected_workspace)
        new-action (actions/insert-one
                     selected-workspace
                     {:name (:name body)
                      :description (:description body)
                      :type (:type body)
                      :config (:config body)})]
    (response {:data (first new-action)})))
