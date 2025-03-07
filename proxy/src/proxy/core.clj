(ns proxy.core
  (:gen-class)
  (:require [aleph.http :as http]
            [manifold.deferred :as d]
            [clojure.core.async :as async]
            [taoensso.telemere :as t]
            [proxy.appconfig :as appconfig]
            [proxy.duckt-server :as duckt-server]
            [clojure.string :as string])
  (:import [java.time Instant ZoneOffset]))

(def ^{:doc
       "State of the proxy.
       fields:
       -> :proxy-id - the id of the proxy
       -> :target-url - the url of the target server
       -> :proxy-secret - the secret
       -> :response-headers-keys - the keys of the response headers to store
       -> :request-headers-keys - the keys of the request headers to store"}
  state (atom {}))

;; get the keys of the headers from the store and return only the headers in those keys
(defmulti get-headers (fn [capture-type _ _] capture-type))

(defmethod get-headers :all [_ headers _]
  (t/log! :debug "get-headers: all")
  headers)
(defmethod get-headers :none [_ & _] {})

(defmethod get-headers :partial [_ headers state-keys]
  (t/log! :debug "get-headers: default - partial")
  (reduce
    (fn [acc k]
      (if-let [header-value (get headers k)]
        (assoc acc k header-value)
        acc))
    {} state-keys))

;; partial is default
(defmethod get-headers :default [_ headers state-keys]
  (get-headers :partial headers state-keys))

(defn save-request! [request response {:keys [start-time]}]
  (t/log! :debug "save-request!")
  (let [req-headers-capture-type (-> @state :request-headers-config :capture_type)
        req-headers-keys (-> @state :request-headers-config :keys)
        res-headers-capture-type (-> @state :response-headers-config :capture_type)
        res-headers-keys (-> @state :response-headers-config :keys)
        request-data {:type "proxy"
                      :uri (:uri request)
                      :host (:url request)
                      :query-params (:query-string request)
                      :status-code (:status response)
                      :request-headers (get-headers
                                         (keyword req-headers-capture-type)
                                         (:headers request)
                                         req-headers-keys)
                      :response-headers (get-headers
                                          (keyword res-headers-capture-type)
                                          (:headers response)
                                          res-headers-keys)
                      :method (:request-method request)
                      :created-at start-time
                      :response-time (-> (Instant/now)
                                         (.atZone ZoneOffset/UTC))}]

    (duckt-server/send-request request-data {:proxy-id (:proxy-id @state)
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

(defn build-state [new-state]
  (reset! state new-state))

(defn -main []
  (t/set-min-level! (keyword appconfig/log-level))
  (t/log! :info "Starting proxy server")
  (let [token (or appconfig/proxy-token
                  (throw (ex-info "Proxy token not set" {})))
        splitted-token (string/split token #":")
        server-config (duckt-server/set-alive!
                        (nth splitted-token 1)
                        (nth splitted-token 2))]
    (build-state {:proxy-id (nth splitted-token 1)
                  :target-url (-> server-config :data :target_url)
                  :request-headers-config (-> server-config :data :request_headers_config)
                  :response-headers-config (-> server-config :data :response_headers_config)
                  :proxy-secret (nth splitted-token 2)})
    (t/log! :info (str "Proxy server started at port " appconfig/proxy-port))

    (start-proxy! (Integer. appconfig/proxy-port) (:target-url @state))))
