(ns server.server
  (:require
    [compojure.core :refer [defroutes GET POST PUT DELETE context]]
    [compojure.handler :as handler]
    [compojure.route :as route]
    [cheshire.generate :refer [add-encoder]]
    [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
    [ring.middleware.cookies :as cookies]
    [jumblerg.middleware.cors :refer [wrap-cors]]
    [ring.util.response :refer [response]]
    [ring.adapter.jetty :as jetty]
    [taoensso.telemere :as t]
    ;; local imports
    [server.appconfig :as appconfig]
    [server.database :as db]
    ;; middlewares
    [server.middlewares.context :as middlewares.context]
    [server.middlewares.auth :as middlewares.auth]
    ;; models
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

(defn server-info [_]
  ;; get cookies
  (t/log! :debug "server info")
  (response {:server "Duckt Server"
             :invite-only? appconfig/invite-only?}))

(defn routes-handler
  ([handler] (routes-handler handler {} {}))
  ([handler params] (routes-handler handler params {}))
  ([handler params query-params]
   (fn [req]
     (t/log! :debug "Route handler")
     (if-let [claims (middlewares.auth/authenticate-token req)]
       (let [context (middlewares.context/user claims)]
         (handler (assoc req :context context) params query-params))
       (-> (response {:error "Unauthorized"})
           (assoc :status 401))))))

(defn proxy-handler
  ([handler] (proxy-handler handler {} {}))
  ([handler params] (proxy-handler handler params {}))
  ([handler params query-params]
   (fn [req]
     (t/log! :debug "Proxy handler")
     (if-let [req-with-context (middlewares.auth/authenticate-proxy req)]
       (handler req-with-context params query-params)
       (-> (response {:error "Unauthorized"})
           (assoc :status 401))))))

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
           {:user-id user-id}))
    (DELETE "/:user-id" [user-id]
            (routes-handler
              users/delete-one
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
