(ns webclient.events.hosts
  (:require [re-frame.core :as rf]))

(rf/reg-event-fx
  :hosts->get-host-by-endpoint-id
  (fn [_ [_ endpoint-id]]
    {:fetch {:uri "/hosts"
             :method "GET"
             :query-params {:endpoint-id endpoint-id}
             :success-fx [:hosts->set-host]}}))

(rf/reg-event-db
  :hosts->set-host
  (fn [db [_ host]]
    (assoc db :hosts->host host)))

(rf/reg-sub
  :hosts->host
  (fn [db _]
    (:hosts->host db)))
