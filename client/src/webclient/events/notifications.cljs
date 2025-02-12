(ns webclient.events.notifications
  (:require
    [re-frame.core :as rf]))

(rf/reg-event-fx
  :notifications->success
  (fn [_ [_ {:keys [message title]} & _]]
    {:notification {:message message
                    :color :teal
                    :position :top-right
                    :title title}}))

(rf/reg-event-fx
  :notifications->failure
  (fn [_ [_ {:keys [message title]} & _]]
    {:notification {:message message
                    :color :red
                    :position :top-right
                    :title title}}))
