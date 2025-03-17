(ns server.core
  (:gen-class)
  (:require
    [taoensso.telemere :as t]
    [server.appconfig :as appconfig]
    [server.server :as server]))

(defn -main []
  ;; TODO set it at config level
  (t/set-min-level! (keyword appconfig/log-level))
  (t/log! :info "Starting Duckt Server...")
  (server/start!))
