(ns server.core
  (:gen-class)
  (:require
    [taoensso.telemere :as t]
    [server.server :as server]))

(defn -main []
  ;; TODO set it at config level
  (t/set-min-level! :debug)
  (t/log! :info "Starting Duckt Server...")
  (server/start!))
