(ns server.api.proxies
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

(defn get-proxy-by-id [req params & _]
  (t/log! :debug "Getting proxy by id")
  (let [context (:context req)
        proxy-id (:proxy-id params)
        selected-workspace (-> context :user-preferences :selected_workspace)]
    (response {:status "ok"
               :data (proxies/get-proxy selected-workspace proxy-id)})))

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
                     :host-url (:host-url body)
                     :target-url (:target-url body)})]
    (response {:status "ok"
               :data (merge
                       {:proxy-key new-key}
                       (first new-proxy))})))

(defn generate-proxy-key [req params & _]
  (t/log! :debug "Generating proxy key")
  (let [new-key (apply str (repeatedly 20 #(rand-nth "abcdefghijklmnopqrstuvwxyz0123456789")))
        hashed-key (buddy/derive new-key)
        proxy-id (:proxy-id params)
        context (:context req)
        selected-workspace (-> context :user-preferences :selected_workspace)]
    (proxies/generate-new-proxy-key
      proxy-id
      selected-workspace
      hashed-key)
    (response {:status "ok"
               :data {:proxy-key (str "v1:" proxy-id ":" new-key)}})))

(defn update-proxy-by-id [req params & _]
  (t/log! :debug "Updating proxy")
  (let [context (:context req)
        body (:body req)
        proxy-id (:proxy-id params)
        selected-workspace (-> context :user-preferences :selected_workspace)]
    (response {:status "ok"
               :data (proxies/update-proxy-by-id
                      selected-workspace
                      proxy-id
                      {:name (:name body)
                       :description (:description body)
                       :target-url (:target-url body)
                       :host-url (:host-url body)})})))

(defn delete-proxy-by-id [req params & _]
  (t/log! :debug "Deleting proxy")
  (println :delete :proxy)
  (let [context (:context req)
        proxy-id (:proxy-id params)
        selected-workspace (-> context :user-preferences :selected_workspace)]
    (response {:status "ok"
               :data (proxies/delete-proxy proxy-id
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
