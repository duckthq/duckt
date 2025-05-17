(ns dev
  (:require
    [server.core :as server]))

(defn start []
  (server/-main))

(defn stop []
  (server/stop!))

(defn restart []
  (stop)
  (start))
