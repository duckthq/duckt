(ns webclient.events.customers
  (:require
    [re-frame.core :as rf]))

(rf/reg-event-fx
  :customers->get
  (fn [{:keys [db]} _]
    {:fetch {:uri "/customers"
             :method "GET"
             :success-fxs [[:customers->set]]}
     :db (assoc db :customers {:loading? true
                               :data (-> db :customers :data)})}))

(rf/reg-event-db
  :customers->set
  (fn [db [_ customers]]
    (assoc db :customers {:loading? false
                          :data (:data customers)})))

(rf/reg-event-fx
  :customers->get-most-active
  (fn [{:keys [db]} _]
    {:fetch {:uri "/customers"
             :method "GET"
             :query-params {:limit 5
                            :order_by "hit_count"}
             :success-fxs [[:customers->set-most-active]]}
     :db (assoc db :customers {:loading? true
                               :data (-> db :customers :data)})}))

(rf/reg-event-db
  :customers->set-most-active
  (fn [db [_ customers]]
    (assoc db :customers->most-active {:loading? false
                          :data (:data customers)})))

(rf/reg-sub
  :customers
  (fn [db _]
    (get-in db [:customers])))

(rf/reg-sub
  :customers->most-active
  (fn [db _]
    (get-in db [:customers->most-active])))
