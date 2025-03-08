(ns server.api.proxies
  (:require
    [ring.util.response :refer [response]]
    [buddy.hashers :as buddy]
    [cheshire.core :as json :refer [generate-string]]
    [crypto.random :as random]
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
        new-key (random/base64 32)
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
                       {:proxy-key (str "v1:" (:id (first new-proxy)) ":" new-key)}
                       (first new-proxy))})))

(defn generate-proxy-key [req params & _]
  (t/log! :debug "Generating proxy key")
  (let [new-key (random/base64 32)
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
    (response
      {:status "ok"
       :data (proxies/update-proxy-by-id
               selected-workspace
               proxy-id
               (merge
                 (when-let [name (:name body)] {:name name})
                 (when-let [description (:description body)] {:description description})
                 (when-let [request-headers-config (:request-headers-config body)]
                   {:request-headers-config (generate-string request-headers-config)})
                 (when-let [response-headers-config (:response-headers-config body)]
                   {:response-headers-config (generate-string response-headers-config)})
                 (when-let [target-url (:target-url body)] {:target-url target-url})
                 (when-let [host-url (:host-url body)] {:host-url host-url})))})))

(defn delete-proxy-by-id [req params & _]
  (t/log! :debug "Deleting proxy")
  (let [context (:context req)
        proxy-id (:proxy-id params)
        selected-workspace (-> context :user-preferences :selected_workspace)]
    (response {:status "ok"
               :data (proxies/delete-proxy proxy-id
                                           selected-workspace)})))

(def ^:private default-headers-capture-keys
  '(:duckt-user-sub :duckt-session-id))

(defn set-proxy-alive [req & _]
  (let [context (:proxy-context req)
        proxy-id (:id context)
        selected-workspace (:workspace-id context)
        proxy-config (first (proxies/update-proxy-status
                              selected-workspace
                              proxy-id
                              "ready"))]
    (t/log! :debug (str "Setting proxy alive for " proxy-id))
    (response
      {:status "ok"
       :data {:target_url (:target_url proxy-config)
              :description (:description proxy-config)
              :request_headers_config {:keys (flatten
                                               (conj
                                                 (-> proxy-config
                                                     :request_headers_config :keys)
                                                 default-headers-capture-keys))
                                       :capture_type (or (-> proxy-config
                                                             :request_headers_config
                                                             :capture_type)
                                                         "partial")}
              :response_headers_config {:keys (flatten
                                                (conj (-> proxy-config
                                                          :response_headers_config :keys)
                                                      :content-type))
                                        :capture_type (or (-> proxy-config
                                                              :response_headers_config
                                                              :capture_type)
                                                          "partial")}}})))
