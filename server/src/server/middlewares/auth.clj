(ns server.middlewares.auth
  (:require
    [taoensso.telemere :as t]
    [buddy.sign.jwt :as jwt]
    [buddy.hashers :as buddy]
    [server.models.proxies :as proxies-model]
    [server.appconfig :as appconfig]))

(defn- verify-jwt-token [token]
  (try
    (jwt/unsign token appconfig/jwt-secret-key)
    (catch Exception e
      (t/log! :error (str "Error verifying token: " e))
      nil)))

(defn authenticate-token [req]
  (t/log! :debug "Authenticating")
  (when-let [token (:value (get-in req [:cookies "token"]))]
    (when-let [claims (verify-jwt-token token)]
      claims)))

(defn authenticate-proxy [req]
  (t/log! :debug "Authenticating proxy")
  (let [token (get-in req [:headers "proxy-secret"])
        proxy-id (get-in req [:headers "proxy-id"])
        $proxy (proxies-model/get-proxy-by-id proxy-id)
        verify (buddy/verify token (:proxy_key_hash $proxy))]
    (when (:valid verify)
      (assoc req :proxy-context {:workspace-id (:workspace_id $proxy)
                                 :name (:name $proxy)
                                 :id (:id $proxy)}))))
