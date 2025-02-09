(ns webclient.events.serverinfo
  (:require
      [re-frame.core :as rf]))

(rf/reg-event-fx
  :serverinfo->get
  (fn [{:keys [db]} [_]]
    {:fetch {:uri "/serverinfo"
             :method "GET"
             :success-fx [:serverinfo->set]}}))

(rf/reg-event-fx
  :serverinfo->set
  (fn [{:keys [db]} [_ serverinfo]]
    {:db (assoc db :serverinfo serverinfo)}))

(rf/reg-sub
  :serverinfo
  (fn [db _]
    (:serverinfo db)))
