(ns webclient.events.auth
  (:require
   [re-frame.core :as rf]))

(rf/reg-event-fx
 :auth->logout
 (fn [_ [_]]
   {:fetch {:uri "/logout"
            :method "POST"
            :body {}
            :success-fxs [[:auth->logout-success]]}}))

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
            :success-fxs [[:auth->signup-success]]
            :failure-fxs [[:auth->signup-failure]]}}))

(rf/reg-event-fx
  :auth->signup-success
  (fn [_ [_]]
    {:navigate {:handler :home}}))

(rf/reg-event-fx
  :auth->signup-failure
  (fn [_ [_ error]]
    {:fx [[:dispatch [:notifications->failure
                      {:title "Signup failed"
                       :message (:error error)}]]]}))

(rf/reg-event-fx
  :auth->login
  (fn [_ [_ form-info]]
    {:fetch {:uri "/login"
             :method "POST"
             :body form-info
             :failure-fxs [[:auth->login-failure]]
             :success-fxs [[:auth->login-success]]}}))

(rf/reg-event-fx
  :auth->login-success
  (fn [_ [_]]
    {:navigate {:handler :home}}))

(rf/reg-event-fx
  :auth->login-failure
  (fn [_ [_ error]]
    {:fx [[:dispatch [:notifications->failure
                      {:title "Log in failed"
                       :message (:error error)}]]]}))
