(ns server.controllers.endpoints
  (:require
    [taoensso.telemere :as t]
    [cheshire.core :refer [generate-string]]
    [server.models.endpoints :as endpoints-model]))

(defn get-endpoints [req & _]
  (println :req req)
  (t/log! :debug "Getting endpoints")
  (let [context (:context req)
        _ (println :context context)
        endpoints-model (endpoints-model/get-endpoints)]
    (generate-string {:status "ok"
                      :data endpoints-model})))

(defn get-endpoint-by-id [req params & _]
  (let [context (:context req)
        endpoint-id (:endpoint-id params)
        _ (t/log! :debug (str "Getting endpoint by id" endpoint-id))
        endpoint-model (endpoints-model/get-endpoint-by-id endpoint-id)]
    (generate-string {:status "ok"
                      :data endpoint-model})))
