(ns server.controllers.proxies
  (:require
    [ring.util.response :refer [response]]
    [buddy.hashers :as buddy]
    [server.models.proxies :as proxies]
    [taoensso.telemere :as t]))

(defn get-proxies [req & _]
  (t/log! :debug "Getting proxies")
  (let [context (:context req)
        selected-workspace (-> context :user-preferences :selected_workspace)
        proxies (proxies/get-proxies selected-workspace)]
    (response {:status "ok"
               :data proxies})))

(defn create-proxy [req & _]
  (t/log! :debug "Creating proxy")
  (let [context (:context req)
        body (:body req)
        selected-workspace (-> context :user-preferences :selected_workspace)
        new-key (apply str (repeatedly 20 #(rand-nth "abcdefghijklmnopqrstuvwxyz0123456789")))
        hashed-key (buddy/derive new-key)
        new-proxy (proxies/create-proxy
                    selected-workspace
                    {:name (:name body)
                     :proxy-key-hash hashed-key
                     :description (:description body)
                     :host_url (:host-url body)
                     :target-url (:target-url body)})]
    (response {:status "ok"
               :data (merge
                       {:proxy-key new-key}
                       (first new-proxy))})))

(defn update-proxy-by-id [req & _]
  (t/log! :debug "Updating proxy")
  (let [context (:context req)
        body (:body req)
        selected-workspace (-> context :user-preferences :selected-workspace)]
    (response {:status "ok"
               :data (proxies/update-proxy-by-id
                      selected-workspace
                      (:id body)
                      {:name (:name body)
                       :description (:description body)
                       :target-url (:target-url body)})})))

(defn delete-proxy-by-id [req & _]
  (t/log! :debug "Deleting proxy")
  (let [context (:context req)
        body (:body req)
        selected-workspace (-> context :user-preferences :selected-workspace)]
    (response {:status "ok"
               :data (proxies/delete-proxy (:id body)
                                           selected-workspace)})))
(defn set-proxy-alive [req & _]
  (t/log! :debug "Setting proxy alive")
  (let [context (:proxy-context req)
        proxy-id (:id context)
        selected-workspace (:workspace-id context)]
    (t/log! :debug (str "Setting proxy alive for " proxy-id))
    (response {:status "ok"
               :data (first (proxies/update-proxy-status
                              selected-workspace
                              proxy-id
                              "ready"))})))
