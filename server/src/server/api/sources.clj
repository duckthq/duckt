(ns server.api.sources
  (:require
    [ring.util.response :refer [response]]
    [buddy.hashers :as buddy]
    [cheshire.core :as json :refer [generate-string]]
    [crypto.random :as random]
    [server.models.sources :as sources]
    [taoensso.telemere :as t]))

(defn get-sources [req & _]
  (t/log! :debug "Getting sources")
  (let [context (:context req)
        selected-workspace (-> context :user-preferences :selected_workspace)
        sources (sources/get-sources selected-workspace)]
    (response {:status "ok"
               :data sources})))

(defn get-source-by-id [req params & _]
  (t/log! :debug "Getting source by id")
  (let [context (:context req)
        source-id (:source-id params)
        selected-workspace (-> context :user-preferences :selected_workspace)]
    (response {:status "ok"
               :data (sources/get-source selected-workspace source-id)})))

(defn create-source [req & _]
  (t/log! :debug "Creating source")
  (let [context (:context req)
        body (:body req)
        selected-workspace (-> context :user-preferences :selected_workspace)
        new-key (random/base64 32)
        hashed-key (buddy/derive new-key)
        new-source (sources/create-source
                    selected-workspace
                    {:name (:name body)
                     :source-key-hash hashed-key
                     :description (:description body)})]
    (response {:status "ok"
               :data (merge
                       {:source-key (str "v1:" (:id (first new-source)) ":" new-key)}
                       (first new-source))})))

(defn generate-source-key [req params & _]
  (t/log! :debug "Generating source key")
  (let [new-key (random/base64 32)
        hashed-key (buddy/derive new-key)
        source-id (:source-id params)
        context (:context req)
        selected-workspace (-> context :user-preferences :selected_workspace)]
    (sources/generate-new-source-key
      source-id
      selected-workspace
      hashed-key)
    (response {:status "ok"
               :data {:source-key (str "v1:" source-id ":" new-key)}})))

(defn update-source-by-id [req params & _]
  (t/log! :debug "Updating source")
  (let [context (:context req)
        body (:body req)
        source-id (:source-id params)
        selected-workspace (-> context :user-preferences :selected_workspace)]
    (response
      {:status "ok"
       :data (sources/update-source-by-id
               selected-workspace
               source-id
               (merge
                 (when-let [name (:name body)] {:name name})
                 (when-let [description (:description body)] {:description description})
                 (when-let [request-headers-config (:request-headers-config body)]
                   {:request-headers-config (generate-string request-headers-config)})
                 (when-let [response-headers-config (:response-headers-config body)]
                   {:response-headers-config (generate-string response-headers-config)})
                 (when-let [target-url (:target-url body)] {:target-url target-url})
                 (when-let [host-url (:host-url body)] {:host-url host-url})))})))

(defn delete-source-by-id [req params & _]
  (t/log! :debug "Deleting source")
  (let [context (:context req)
        source-id (:source-id params)
        selected-workspace (-> context :user-preferences :selected_workspace)]
    (response {:status "ok"
               :data (sources/delete-source source-id
                                           selected-workspace)})))

(def ^:private default-headers-capture-keys
  '(:duckt-user-sub :duckt-session-id))

(defn set-source-alive [req & _]
  (let [context (:source-context req)
        source-id (:id context)
        selected-workspace (:workspace-id context)
        source-config (first (sources/update-source-status
                              selected-workspace
                              source-id
                              "ready"))]
    (t/log! :debug (str "Setting source alive for " source-id))
    (response
      {:status "ok"
       :data {:target_url (:target_url source-config)
              :description (:description source-config)
              :request_headers_config {:keys (flatten
                                               (conj
                                                 (-> source-config
                                                     :request_headers_config :keys)
                                                 default-headers-capture-keys))
                                       :capture_type (or (-> source-config
                                                             :request_headers_config
                                                             :type)
                                                         "partial")}
              :response_headers_config {:keys (flatten
                                                (conj (-> source-config
                                                          :response_headers_config :keys)
                                                      :content-type))
                                        :capture_type (or (-> source-config
                                                              :response_headers_config
                                                              :type)
                                                          "partial")}}})))
