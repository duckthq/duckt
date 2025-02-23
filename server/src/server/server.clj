(ns server.server
  (:require
    [compojure.core :refer [defroutes GET POST PUT DELETE context]]
    [compojure.handler :as handler]
    [compojure.route :as route]
    [cheshire.generate :refer [add-encoder]]
    [buddy.hashers :as buddy]
    [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
    [ring.middleware.cookies :as cookies]
    [jumblerg.middleware.cors :refer [wrap-cors]]
    [ring.util.response :refer [response]]
    [ring.adapter.jetty :as jetty]
    [taoensso.telemere :as t]
    [buddy.sign.jwt :as jwt]
    ;; local imports
    [server.appconfig :as appconfig]
    [server.database :as db]
    ;; models
    [server.models.users :as users-model]
    [server.models.proxies :as proxies-model]
    ;; controllers
    [server.api.auth :as auth]
    [server.api.workspaces :as workspaces]
    [server.api.user :as user-ctrl]
    [server.api.users :as users]
    [server.api.proxies :as proxies]
    [server.api.requests :as requests]
    [server.api.endpoints :as endpoints-ctrl]
    [server.api.customers :as customers]))

;; Add encoder for java time lib on the responses
(add-encoder java.time.OffsetDateTime cheshire.generate/encode-str)
(add-encoder java.time.Instant cheshire.generate/encode-str)
(add-encoder java.time.LocalDateTime cheshire.generate/encode-str)

(defn verify-jwt-token [token]
  (try
    (jwt/unsign token appconfig/jwt-secret-key)
    (catch Exception e
      (t/log! :error (str "Error verifying token: " e))
      nil)))

(defn authenticate [req]
    (t/log! :debug "Authenticating")
    (if-let [token (:value (get-in req [:cookies "token"]))]
      (if-let [claims (verify-jwt-token token)]
        ;; TODO: get workspace and workspace permissions
        (let [context (users-model/build-user-context (:sub claims))]
          (t/log! :debug (str "Authenticated user: " (:user context)))
          (assoc req :context context))
        (-> (response {:error "Invalid token"})
            (assoc :status 401)))
      (-> (response {:error "No token provided"})
          (assoc :status 401))))

(defn server-info [_]
  ;; get cookies
  (t/log! :debug "server info")
  (response {:server "Duckt Server"}))

(defn authenticate-proxy [req]
  (t/log! :debug "Authenticating proxy")
  (let [token (get-in req [:headers "proxy-secret"])
        proxy-id (get-in req [:headers "proxy-id"])
        $proxy (proxies-model/get-proxy-by-id proxy-id)
        verify (buddy/verify token (:proxy_key_hash $proxy))]
    (if (:valid verify)
      (assoc req :proxy-context {:workspace-id (:workspace_id $proxy)
                                 :name (:name $proxy)
                                 :id (:id $proxy)})
      (-> (response {:error "Invalid proxy token"})
          (assoc :status 401)))))

(defn routes-handler
  ([handler] (routes-handler handler {} {}))
  ([handler params] (routes-handler handler params {}))
  ([handler params query-params]
   (fn [req]
     (t/log! :debug "Route handler")
     (-> req
         authenticate
         (handler params query-params)))))

(defn proxy-handler
  ([handler] (proxy-handler handler {} {}))
  ([handler params] (proxy-handler handler params {}))
  ([handler params query-params]
   (fn [req]
     ;(t/log! :debug (str "Params: " (:params req)))
     (t/log! :debug "Proxy handler")
     (-> req
         authenticate-proxy
         (handler params query-params)))))

(defroutes app-routes
  (POST "/signup" [] auth/signup)
  (POST "/login" [] auth/login)
  (POST "/logout" [] auth/logout)
  (GET "/serverinfo" [] server-info)
  (GET "/userinfo" []
       (routes-handler
         user-ctrl/user-info))

  (context "/users" []
    (GET "/" []
         (routes-handler
           users/get-all))
    (POST "/" []
          (routes-handler
            users/create-one))
    (PUT "/:user-id" [user-id]
         (routes-handler
           users/update-one
           {:user-id user-id}))
    (PUT "/:user-id/role" [user-id]
         (routes-handler
           users/update-role
           {:user-id user-id})))

  (GET "/workspaces" []
       (routes-handler
         workspaces/get-workspaces))
  (POST "/workspaces" []
        (routes-handler
          workspaces/create-workspace))

  (GET "/proxies" []
       (routes-handler
         proxies/get-proxies))
  (GET "/proxies/:proxy-id" [proxy-id]
       (routes-handler
         proxies/get-proxy-by-id
         {:proxy-id proxy-id}))
  (GET "/proxies/:proxy-id/requests" [proxy-id]
       (routes-handler
         requests/get-requests-by-proxy-id
         {:proxy-id proxy-id}))
  (GET "/proxies/:proxy-id/requests-timeframe" [proxy-id]
       (routes-handler
         requests/get-timeframe-requests-by-proxy
         {:proxy-id proxy-id}))

  (GET "/proxies/:proxy-id/requests/:request-id" [proxy-id request-id]
       (routes-handler
         requests/get-one
         {:proxy-id proxy-id
          :request-id request-id}))
  (POST "/proxies" []
        (routes-handler
          proxies/create-proxy))
  (PUT "/proxies/:proxy-id" [proxy-id]
       (routes-handler
         proxies/update-proxy-by-id
         {:proxy-id proxy-id}))
  (POST "/proxies/:proxy-id/generate-key" [proxy-id]
       (routes-handler
         proxies/generate-proxy-key
         {:proxy-id proxy-id}))

  (DELETE "/proxies/:proxy-id" [proxy-id]
          (routes-handler
            proxies/delete-proxy-by-id
            {:proxy-id proxy-id}))

  (GET "/requests/:request-id" [request-id]
       (routes-handler
         requests/get-one
         {:request-id request-id}))

  (GET "/endpoints" []
       (routes-handler
         endpoints-ctrl/get-endpoints))

  (GET "/endpoints/:endpoint-id" [endpoint-id]
       (routes-handler
         endpoints-ctrl/get-endpoint-by-id
         {:endpoint-id endpoint-id}))

  (GET "/customers" []
       (routes-handler
         customers/list-customers))

  (context
    "/p" []
    (POST "/requests" []
          (proxy-handler
            requests/register-request))
    (POST "/alive" []
          (proxy-handler
            proxies/set-proxy-alive)))

  (route/not-found "Not Found"))

(def app
  (-> (handler/site app-routes)
      cookies/wrap-cookies
      ;; TODO make cors configurable
      (wrap-cors #".*")
      (wrap-json-body {:keywords? true})
      wrap-json-response))
      ;;(wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))))

(defn start! []
  (db/initialize!)
  (t/log! :info (str "The server was started at port " appconfig/port))
  (jetty/run-jetty app {:port appconfig/port}))
