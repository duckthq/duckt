(ns webclient.events.system
  (:require
    [re-frame.core :as rf]))

(rf/reg-event-fx
  :system->get-actions
  (fn [_ _]
    {:fetch {:uri "/system/actions"
             :method "GET"
             :success-fxs [[:system->set-actions]]}}))

(rf/reg-event-db
  :system->set-actions
  (fn [db [_ actions]]
    (assoc db :system/actions actions)))

(rf/reg-sub
  :system/actions
  (fn [db _]
    (:system/actions db)))
