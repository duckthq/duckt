(ns webclient.events.proxies
  (:require
    [re-frame.core :as rf]))

(rf/reg-event-fx
  :proxies->get
  (fn [{:keys [db]} _]
    {:fetch {:uri "/proxies"
             :method "GET"
             :success-fxs [[:proxies->set]]}
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
             :success-fxs [[:proxies->get]
                           [:proxies->create-success]]}}))

(rf/reg-event-fx
  :proxies->update
  (fn [_ [_ proxy-id proxy-info]]
    {:fetch {:uri (str "/proxies/" proxy-id)
             :method "PUT"
             :body proxy-info
             :success-fxs [[:proxies->get]
                           [:notifications->success {:title "Proxy updated!"
                                                     :level :success}]]}}))
(rf/reg-event-fx
  :proxies->delete
  (fn [_ [_ proxy-id]]
    {:fetch {:uri (str "/proxies/" proxy-id)
             :method "DELETE"
             :success-fxs [[:proxies->get]
                           [:notifications->success {:title "Proxy deleted!"
                                                     :level :success}]
                           [:navigate :home]]}}))

(rf/reg-event-fx
  :proxies->create-success
  (fn [{:keys [db]} [_ payload]]
    {:navigate [:new-proxy-success]
     :db (assoc db :proxies->new-proxy-info (:data payload))}))

(rf/reg-event-fx
  :proxies->get-by-id
  (fn [{:keys [db]} [_ proxy-id]]
    {:fetch {:uri (str "/proxies/" proxy-id)
             :method "GET"
             :success-fx [:proxies->set-by-id]}
     :db (assoc db :proxies->proxy-info {:loading? true
                                         :data (-> db :proxies->proxy-info :data)})}))

(rf/reg-event-db
  :proxies->set-by-id
  (fn [db [_ proxy-info]]
    (assoc db :proxies->proxy-info {:loading? false
                                    :data (:data proxy-info)})))

(rf/reg-event-fx
  :proxies->swap-key
  (fn [_ [_ proxy-id]]
    {:fetch {:uri (str "/proxies/" proxy-id "/generate-key" )
             :method "POST"
             :success-fxs [[:proxies->get]
                           [:proxies->set-swapped-key]
                           [:modal->close]
                           [:notifications->success
                            {:title "Key swapped!"
                             :level :success}]]}}))

(rf/reg-event-fx
  :proxies->clean-swapped-key-from-memory
  (fn [{:keys [db]} _]
    {:db (assoc db :proxies->swapped-key nil)}))

(rf/reg-event-db
  :proxies->set-active-proxy
  (fn [db [_ proxy-id]]
    (assoc db :proxies->active-proxy proxy-id)))

(rf/reg-event-db
  :proxies->set-swapped-key
  (fn [db [_ new-key]]
    (assoc db :proxies->swapped-key (-> new-key :data :proxy-key))))

(rf/reg-sub
  :proxies->swapped-key
  (fn [db _]
    (:proxies->swapped-key db)))

(rf/reg-sub
  :proxies->proxy-info
  (fn [db _]
    (:proxies->proxy-info db)))

(rf/reg-sub
  :proxies->new-proxy-info
  (fn [db _]
    (:proxies->new-proxy-info db)))

(rf/reg-sub
  :proxies
  (fn [db _]
    (:proxies db)))

(rf/reg-sub
  :proxies->active-proxy
  (fn [db _]
    (:proxies->active-proxy db)))
