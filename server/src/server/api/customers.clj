(ns server.api.customers
  (:require
    [ring.util.response :refer [response]]
    [cheshire.core :refer [generate-string]]
    [taoensso.telemere :as t]
    [server.models.customers :as customers-model]))

(defn list-customers [req & params]
  (t/log! :debug "Listing customers")
  (let [customers (customers-model/list-customers)]
    (generate-string
      {:status "ok"
       :data customers})))
