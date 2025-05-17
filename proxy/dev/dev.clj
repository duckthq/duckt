(ns dev
  (:require
    [proxy.core :as p]))

(defn start []
  (p/-main))

(defn stop []
  (p/stop!))

(defn restart []
  (stop)
  (start))
