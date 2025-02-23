(ns webclient.events.users
  (:require
   [re-frame.core :as rf]))

(rf/reg-event-fx
  :users->get
  (fn [_ [_]]
    {:fetch {:uri "/users"
             :method "GET"
             :success-fxs [[:user->set]]}}))

(rf/reg-event-fx
  :users->add
  (fn [{:keys [db]} [_ payload]]
    {:fetch {:uri "/users"
             :method "POST"
             :body payload
             :success-fxs [[:users->new-user-success]
                           [:users->get]]}
     :db (assoc db :users->new-user-information
                {:data nil
                 :status :loading})}))

(rf/reg-event-fx
  :users->new-user-success
  (fn [{:keys [db]} [_ payload]]
    {:db (assoc db :users->new-user-information
                {:data (:data payload)
                 :status :success})}))

(rf/reg-event-fx
  :users->reset-new-user-information
  (fn [{:keys [db]} [_]]
    (println :reset)
    {:db (assoc db :users->new-user-information
                {:data nil
                 :status nil})}))

(rf/reg-event-db
  :user->set
  (fn [db [_ users]]
    (assoc db :users users)))

(rf/reg-sub
  :users
  (fn [db _]
    (:users db)))

(rf/reg-sub
  :users->new-user-information
  (fn [db _]
    (:users->new-user-information db)))
