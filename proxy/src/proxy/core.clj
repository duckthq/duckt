(ns proxy.core
  (:gen-class)
  (:require [aleph.http :as http]
            [proxy.http :as proxy-http]
            [taoensso.telemere :as t]
            [proxy.appconfig :as appconfig]))

(defn start-proxy! [port upstream-url]
  (http/start-server
    (proxy-http/proxy-handler upstream-url)
    {:port port
     :executor :none
     :raw-stream? true}))

(defonce http-server-instance (atom nil))

(defn stop! []
  (when-let [server @http-server-instance]
    (.close server)
    (reset! http-server-instance nil)))

(defn start! []
  (when @http-server-instance
    (stop!))
  (t/set-min-level! (keyword appconfig/log-level))
  (t/log! :info "Starting proxy server")
  (let [server (start-proxy!
                 (Integer. appconfig/http-proxy-port)
                 "https://mat-m.com")]
    (reset! http-server-instance server)
    (t/log! :info (str "Proxy server started at port "
                       appconfig/http-proxy-port))))

(comment
  (start!)
  (stop!))

(defn -main []
  (start!))
