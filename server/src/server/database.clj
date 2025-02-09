(ns server.database
  (:require
    [pg.core :as pg2]
    [pg.migration.core :as migration]
    [taoensso.telemere :as t]
    [server.appconfig :as appconfig]
    ))

(def config
  {:host (:host appconfig/postgres)
   :port (:port appconfig/postgres)
   :user (:user appconfig/postgres)
   :password (:password appconfig/postgres)
   :database (:database appconfig/postgres)
   :migrations-table :migrations})

;; TODO: manage connection pool
(def conn (atom nil))

(defn connection []
  (pg2/connect config))

(defn- migrate! []
  (t/log! :info "Migrating database")
  (migration/migrate-all config))

(defn initialize! []
  (t/log! :info "Initializing database")
  (let [db-conn (pg2/connect config)]
    (migrate!)
    (reset! conn db-conn)))
