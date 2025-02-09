(ns webclient.events.proxies
  (:require
    [re-frame.core :as rf]))

(rf/reg-event-fx
  :proxies->get
  (fn [{:keys [db]} _]
    {:fetch {:uri "/proxies"
             :method "GET"
             :success-fx [:proxies->set]}
     :db (assoc db :proxies {:loading? true
                             :data (-> db :proxies :data)})}))

(rf/reg-event-db
  :proxies->set
  (fn [db [_ proxies]]
    (assoc db :proxies {:loading? false
                        :data (:data proxies)})))

(rf/reg-event-fx
  :proxies->create
  (fn [_ [_ new-proxy]]
    {:fetch {:uri "/proxies"
             :method "POST"
             :body new-proxy
             :success-fx [:proxies->get
                          :proxies->create-success]}}))

(rf/reg-event-fx
  :proxies->create-success
  (fn [{:keys [db]} [_ payload]]
    {:navigate [:new-proxy-success]
     :db (assoc db :proxies->new-proxy-info (:data payload))}))

(rf/reg-sub
  :proxies->new-proxy-info
  (fn [db _]
    (:proxies->new-proxy-info db)))

(rf/reg-sub
  :proxies
  (fn [db _]
    (:proxies db)))
