(ns proxy.core
  (:gen-class)
  (:require [aleph.http :as http]
            [manifold.deferred :as d]
            [clojure.core.async :as async]
            [taoensso.telemere :as t]
            [proxy.appconfig :as appconfig]
            [proxy.mainframe-client :as mainframe-client]
            [clojure.string :as string])
  (:import [java.time Instant ZoneOffset]))

(def state (atom {}))

(defn save-request! [request response {:keys [start-time]}]
  (t/log! :debug "save-request!")
  (let [request-data {:type "proxy"
                      :uri (:uri request)
                      :host (:url request)
                      :query-params (:query-string request)
                      :status-code (:status response)
                      :request-headers (:headers request)
                      :response-headers (:headers response)
                      :method (:request-method request)
                      :created-at start-time
                      :response-time (-> (Instant/now)
                                         (.atZone ZoneOffset/UTC))}]

    (mainframe-client/send-request request-data {:proxy-id (:proxy-id @state)
                                                 :proxy-secret (:proxy-secret @state)})))

(defn proxy-handler [upstream-url]
  (let [upstream-uri (java.net.URI. upstream-url)
        upstream-host (.getHost upstream-uri)]
  (fn [{:keys [headers request-method] :as request}]
    (let [url (str upstream-url
                   (:uri request)
                   (when (:query-string request)
                     (str "?"
                          (:query-string request))))
          _ (t/log! :debug (str "proxy-handler: handling " url))
          proxy-request {:request-method request-method
                         :uri (:uri request)
                         :url url
                         :scheme (:scheme request)
                         :headers (merge headers
                                         {"host" upstream-host})
                         :body (:body request)
                         :throw-exceptions false
                         :follow-redirects true
                         :allow-unsafe-redirect true
                         ;; TODO: deprecate this in favor of :query-params
                         :query-string (:query-string request)}
          start-time (-> (Instant/now)
                         (.atZone ZoneOffset/UTC))
          response (http/request proxy-request)]
      (d/on-realized response #(save-request! proxy-request % {:start-time start-time})
                     (constantly nil))
      response))))

(defn start-proxy! [port upstream-url]
  (http/start-server (proxy-handler upstream-url) {:port port
                                                   :executor :none
                                                   :raw-stream? true}))

(defn build-state [{:keys [proxy-id proxy-secret target-url]}]
  (reset! state
          {:proxy-id proxy-id
           :target-url target-url
           :proxy-secret proxy-secret}))

(defn -main []
  (t/set-min-level! (keyword appconfig/log-level))
  (t/log! :info "Starting proxy server")
  (t/log! :info (str "Proxy server started at port " appconfig/proxy-port))
  (let [token (or appconfig/proxy-token
                  (throw (ex-info "Proxy token not set" {})))
        splitted-token (string/split token #":")]
    (build-state {:proxy-id (nth splitted-token 1)
                  :target-url (let [response (mainframe-client/set-alive!
                                               (nth splitted-token 1)
                                               (nth splitted-token 2))]
                                (-> response :data :target_url))
                  :proxy-secret (nth splitted-token 2)})
    (t/log! :info "I AM ALIVE!")
    (start-proxy! (Integer. appconfig/proxy-port) (:target-url @state))))
