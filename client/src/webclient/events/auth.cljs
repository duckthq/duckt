(ns webclient.events.auth
  (:require
   [re-frame.core :as rf]))

(rf/reg-event-fx
 :auth->logout
 (fn [_ [_]]
   {:fetch {:uri "/logout"
            :method "POST"
            :body {}
            :success-fx [:auth->logout-success]}}))

(rf/reg-event-fx
 :auth->logout-success
 (fn [_ [_]]
   {:navigate {:handler :login}}))

(rf/reg-event-fx
 :auth->signup
 (fn [_ [_ form-info]]
   {:fetch {:uri "/signup"
            :method "POST"
            :body form-info
            :success-fx [:auth->set-token :auth->signup-success]}}))

(rf/reg-event-fx
 :auth->set-token
 (fn [{:keys [db]} [_]]
   {:db (assoc db :token nil)}))

(rf/reg-event-fx
  :auth->signup-success
  (fn [_ [_]]
    {:navigate {:handler :home}}))

(rf/reg-event-fx
  :auth->login
  (fn [_ [_ form-info]]
    {:fetch {:uri "/login"
             :method "POST"
             :body form-info
             :success-fxs [[:auth->login-success]]}}))

(rf/reg-event-fx
  :auth->login-success
  (fn [_ [_]]
    {:navigate {:handler :home}}))
