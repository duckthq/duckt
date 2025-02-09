(ns webclient.events.endpoints
  (:require
      [re-frame.core :as rf]))

(rf/reg-event-fx
  :endpoints->get
  (fn [{:keys [db]} [_]]
    {:fetch {:uri "/endpoints"
             :method "GET"
             :success-fx [:endpoints->set]}}))

(rf/reg-event-fx
  :endpoints->get-requests
  (fn [{:keys [db]} [_ endpoint-id]]
    {:fetch {:uri (str "/endpoints/" endpoint-id "/requests")
             :method "GET"
             :success-fx [:endpoints->set-requests]}}))

(rf/reg-event-fx
  :endpoints->get-endpoint
  (fn [_ [_ endpoint-id]]
    {:fetch {:uri (str "/endpoints/" endpoint-id)
             :method "GET"
             :success-fx [:endpoints->set-endpoint]}}))

(rf/reg-event-db
  :endpoints->set-endpoint
  (fn [db [_ endpoint]]
    (assoc db :endpoints->endpoint endpoint)))

(rf/reg-event-db
  :endpoints->set-requests
  (fn [db [_ endpoints]]
    (assoc db :endpoints->requests-list endpoints)))

(rf/reg-event-fx
  :endpoints->set
  (fn [{:keys [db]} [_ endpoints]]
    {:db (assoc db :endpoints endpoints)
     :fx []}))

(rf/reg-sub
  :endpoints
  (fn [db _]
    (:endpoints db)))

(rf/reg-sub
  :endpoints->endpoint
  (fn [db _]
    (:endpoints->endpoint db)))

(rf/reg-sub
  :endpoints->requests-list
  (fn [db _]
    (:endpoints->requests-list db)))
