(ns webclient.events.workspaces
  (:require
    [re-frame.core :as rf]))

(rf/reg-event-fx
  :workspaces->get
  (fn [_ _]
    {:fetch {:uri "/workspaces"
             :method "GET"
             :success-fx [:workspaces->set]}}))

(rf/reg-event-db
  :workspaces->set
  (fn [db [_ workspaces]]
    (assoc db :workspaces (:data workspaces))))

(rf/reg-sub
  :workspaces
  (fn [db _]
    (:workspaces db)))
