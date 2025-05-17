(ns webclient.events.sources
  (:require
    [re-frame.core :as rf]))

(rf/reg-event-fx
  :sources->get
  (fn [{:keys [db]} _]
    {:fetch {:uri "/sources"
             :method "GET"
             :success-fxs [[:sources->set]]}
     :db (assoc db :sources {:loading? true
                             :data (-> db :sources :data)})}))

(rf/reg-event-db
  :sources->set
  (fn [db [_ sources]]
    (assoc db :sources {:loading? false
                        :data (:data sources)})))

(rf/reg-event-fx
  :sources->create
  (fn [_ [_ new-source]]
    {:fetch {:uri "/sources"
             :method "POST"
             :body new-source
             :success-fxs [[:sources->get]
                           [:sources->create-success]]}}))

(rf/reg-event-fx
  :sources->update
  (fn [_ [_ source-id source-info]]
    {:fetch {:uri (str "/sources/" source-id)
             :method "PUT"
             :body source-info
             :success-fxs [[:sources->get]]}}))
(rf/reg-event-fx
  :sources->delete
  (fn [_ [_ source-id]]
    {:fetch {:uri (str "/sources/" source-id)
             :method "DELETE"
             :success-fxs [[:sources->get]
                           [:navigate {:handler :home}]]}}))

(rf/reg-event-fx
  :sources->create-success
  (fn [{:keys [db]} [_ payload]]
    {:navigate {:handler :new-source-success}
     :db (assoc db :sources->new-source-info (:data payload))}))

(rf/reg-event-fx
  :sources->get-by-id
  (fn [{:keys [db]} [_ source-id]]
    {:fetch {:uri (str "/sources/" source-id)
             :method "GET"
             :success-fxs [[:sources->set-by-id]]}
     :db (assoc db :sources->source-info {:loading? true
                                         :data (-> db :sources->source-info :data)})}))

(rf/reg-event-db
  :sources->set-by-id
  (fn [db [_ source-info]]
    (assoc db :sources->source-info {:loading? false
                                    :data (:data source-info)})))

(rf/reg-event-fx
  :sources->swap-key
  (fn [_ [_ source-id]]
    {:fetch {:uri (str "/sources/" source-id "/generate-key" )
             :method "POST"
             :success-fxs [[:sources->get]
                           [:sources->set-swapped-key]
                           [:modal->close]
                           [:notifications->success
                            {:title "Key swapped!"
                             :level :success}]]}}))

(rf/reg-event-fx
  :sources->clean-swapped-key-from-memory
  (fn [{:keys [db]} _]
    {:db (assoc db :sources->swapped-key nil)}))

(rf/reg-event-db
  :sources->set-active-source
  (fn [db [_ source-id]]
    (assoc db :sources->active-source source-id)))

(rf/reg-event-db
  :sources->set-swapped-key
  (fn [db [_ new-key]]
    (assoc db :sources->swapped-key (-> new-key :data :source-key))))

(rf/reg-sub
  :sources->swapped-key
  (fn [db _]
    (:sources->swapped-key db)))

(rf/reg-sub
  :sources->source-info
  (fn [db _]
    (:sources->source-info db)))

(rf/reg-sub
  :sources->new-source-info
  (fn [db _]
    (:sources->new-source-info db)))

(rf/reg-sub
  :sources
  (fn [db _]
    (:sources db)))

(rf/reg-sub
  :sources->active-source
  (fn [db _]
    (:sources->active-source db)))
