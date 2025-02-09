(ns server.models.customers
  (:require
    [pg.core :as pg]
    [server.database :as db]))

(defn list-customers []
  (with-open [conn (db/connection)]
    (pg/query conn
                "SELECT * FROM customers")))
