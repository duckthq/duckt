(ns server.appconfig
  (:require [environ.core :refer [env]]))

(def port
  (int (env :port 4444)))

(def jwt-secret-key
  (env :jwt-secret-key "secret"))

(def postgres
  {:host (env :db-host "localhost")
   :port (env :db-port 5432)
   :user (env :db-user "duckt")
   :password (env :db-password "duckt")
   :database (env :db-name "duckt")})
