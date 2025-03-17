(ns webclient.events.requests
  (:require
      [re-frame.core :as rf]))

(rf/reg-event-fx
  :requests->get-by-proxy-id
  (fn [{:keys [db]} [_ proxy-id params]]
    (let [search (.. js/window -location -search)
         url-search-params (new js/URLSearchParams search)
         url-params-list (js->clj (for [q url-search-params] q))
         url-params-map (into (sorted-map) url-params-list)
         query-params (merge url-params-map params)]

      {:fetch {:uri (str "/proxies/" proxy-id "/requests")
               :method "GET"
               :query-params query-params
               :success-fx [:requests->set]}
       :db (assoc db :requests {:loading? true
                                :filters query-params
                                :data (-> db :requests :data)})})))

(rf/reg-event-db
  :requests->set
  (fn [db [_ requests]]
    (assoc db :requests {:loading? false
                         :filters (-> db :requests :filters)
                         :data (:data requests)})))

(rf/reg-event-fx
  :requests->load-more
  (fn [{:keys [db]} [_ proxy-id]]
    (let [filters (-> db :requests :filters)
          limit (get filters "limit")]
      {:fx [[:dispatch [:requests->filter
                        {"limit" (if limit (+ (int limit) 100) 200)}
                        proxy-id]]]})))

(rf/reg-event-fx
  :requests->filter
  (fn [_ [_ filters proxy-id]]
    (let [url (js/URL. js/window.location.href)
          search-params (.-searchParams url)
          search-params-clj (into {} (js->clj (for [q search-params] q)))
          merged-params (merge search-params-clj filters)]
      (doseq [[k v] merged-params]
        (.set search-params (name k) v))
      (.pushState js/history nil "" url))
    {:fx [[:dispatch [:requests->get-by-proxy-id proxy-id]]]}))

(rf/reg-event-fx
  :requests->get-timeframe-by-proxy
  (fn [{:keys [db]} [_ {:keys [proxy-id status-code-group
                               start-time end-time]}]]
    {:fetch {:uri (str "/proxies/" proxy-id "/requests-timeframe")
             :method "GET"
             :query-params (merge
                             (when status-code-group
                               {:status-code-group status-code-group})
                             {:start-time start-time
                              :end-time end-time})
             :success-fx [:requests->set-timeframe]}
     :db (assoc db :requests->timeframe {:loading? true
                                         :data (-> db :requests->timeframe :data)})}))

(rf/reg-event-fx
  :requests->get-timeframes
  (fn [{:keys [db]} [_ {:keys [status-code-group start-time end-time]}]]
    {:fetch {:uri "/requests-timeframe"
             :method "GET"
             :query-params (merge
                             (when status-code-group
                               {:status-code-group status-code-group})
                             {:start-time start-time
                              :end-time end-time})
             :success-fx [:requests->set-timeframe]}
     :db (assoc db :requests->timeframe {:loading? true
                                         :data (-> db :requests->timeframe :data)})}))

(rf/reg-event-fx
  :requests->set-timeframe
  (fn [{:keys [db]} [_ requests]]
    {:db (assoc-in db [:requests->timeframe :loading?] false)
     :fx [[:dispatch [:requests->set-timeframe-data (:data requests)]]]}))

(rf/reg-event-db
  :requests->set-timeframe-data
  (fn [db [_ requests]]
    (assoc-in db
              [:requests->timeframe :data (keyword (:status-code-group requests))]
              requests)))

(rf/reg-event-fx
  :requests->get-request-details
  (fn [{:keys [db]} [_ request-id]]
    {:fetch {:uri (str "/requests/" request-id)
             :method "GET"
             :success-fx [:requests->set-request-details]}
     :db (assoc db :requests->request-details {:loading? true
                                               :data (-> db :requests->request-details :data)})}))

(rf/reg-event-db
  :requests->set-request-details
  (fn [db [_ request-details]]
    (assoc db :requests->request-details {:loading? false
                                          :data (:data request-details)})))

(rf/reg-sub
  :requests->request-details
  (fn [db _]
    (:requests->request-details db)))

(rf/reg-sub
  :requests->timeframe
  (fn [db _]
    (:requests->timeframe db)))

(rf/reg-sub
  :requests
  (fn [db _]
    (:requests db)))
