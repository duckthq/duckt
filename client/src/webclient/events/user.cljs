(ns webclient.events.user
  (:require
   [re-frame.core :as rf]))

(rf/reg-event-fx
  :user->get-userinfo
  (fn [_ [_]]
    {:fetch {:uri "/userinfo"
             :method "GET"
             :success-fx [:user->set-userinfo]}}))

(rf/reg-event-fx
  :user->set-userinfo
  (fn [{:keys [db]} [_ user-info]]
    {:db (assoc db :user->userinfo (:data user-info))}))

(rf/reg-sub
  :user->userinfo
  (fn [db _]
    (:user->userinfo db)))
