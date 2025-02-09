(ns server.appconfig
  (:require [environ.core :refer [env]]))

(def port
  (int (env :port 4444)))

(def jwt-secret-key
  (env :jwt-secret-key "secret"))

(def proxy-port
  (int (env :proxy-port 4445)))
(def proxy-path
  (env :proxy-path "/"))
(def proxy-thread-pool-size
  (int (env :proxy-thread-pool-size 100)))
(def proxy-buffer-size
  (int (env :proxy-buffer-size 8192)))

(def postgres
  {:host (env :db-host "localhost")
   :port (env :db-port 5432)
   :user (env :db-user "mainframe")
   :password (env :db-password "mainframe")
   :database (env :db-name "mainframe")})
