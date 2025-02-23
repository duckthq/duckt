(ns webclient.events.core
  (:require
    [re-frame.core :as rf]
    ["@mantine/notifications" :refer [notifications]]
    ;; events registration
    [webclient.events.auth]
    [webclient.events.serverinfo]
    [webclient.events.workspaces]
    [webclient.events.proxies]
    [webclient.events.user]
    [webclient.events.users]
    [webclient.events.hosts]
    [webclient.events.requests]
    [webclient.events.customers]
    [webclient.events.endpoints]
    [webclient.events.notifications]
    [webclient.events.modal]
    ;; end events registration
    ;; internal
    [webclient.config :as config]
    [webclient.db :as db]))

(rf/reg-event-db
  ::initialize-db
  (fn [_ _]
    db/default-db))

(rf/reg-event-fx
  :navigate
  (fn [_ [_ handler]]
    {:navigate handler}))

(rf/reg-event-fx
  ::set-active-panel
  (fn [{:keys [db]} [_ active-panel]]
    {:db (assoc db :active-panel active-panel)}))

(rf/reg-sub
  :theme
  (fn [db _]
    (:theme db)))

(rf/reg-fx
  :notification
  (fn [props]
  (.show notifications (clj->js props))))

(rf/reg-fx
  :fetch
  (fn [{:keys [method uri query-params body success-fx failure-fx headers
               success-fxs]}]
    (let [json-body (.stringify js/JSON (clj->js body))
          query-params-parser #(let [url-search-params (new js/URLSearchParams (clj->js %))]
                                 (if (and (seq (.toString url-search-params)) %)
                                   (str "?" (.toString url-search-params))
                                   ""))
          not-ok (fn [{:keys [status on-failure]}]
                   (when (= status 401) (let [_ (rf/dispatch [:auth->logout])]))
                   (when (> status 399) (on-failure)))
          req-headers (clj->js (merge {:Accept "application/json"
                                       :duckt-user-sub (js/localStorage.getItem "userEmail")
                                       :Content-Type "application/json"}
                                      headers))]
      ;; fetch the API
      (-> (js/fetch (str config/api-url uri (query-params-parser query-params))
                    (clj->js
                      (merge {:method (or method "GET")
                              :credentials "include"
                              :headers req-headers}
                             (when-let [_ (and (not= method "GET")
                                               (not= method "HEAD"))]
                               {:body json-body}))))
          ;; response parser
          (.then
            (fn [response]
              (-> (.json response)
                  (.then
                    (fn [json]
                      (let [payload (js->clj json :keywordize-keys true)]
                        (when (not (.-ok response))
                          (not-ok {:status (.-status response)
                                   :on-failure #(throw (js/Error. (js/JSON.stringify json)))}))
                        ;; maps every success-fx to a dispatch
                        (mapv
                          #(rf/dispatch [% payload (.-headers response)])
                          success-fx)
                        (mapv
                          #(rf/dispatch (vec (flatten [% payload (.-headers response)])))
                          success-fxs)))))))
          ;; request error handling
          (.catch
            (fn [error]
              (if (= (.-message error) "Failed to fetch")
                (if (or (= failure-fx nil)
                        (= (count failure-fx) 0))
                  (.error js/console (js/JSON.parse (.-message error)))
                  (mapv
                    #(rf/dispatch [% (:message (.-message error))])
                    failure-fx))

                (if (or (= failure-fx nil)
                        (= (count failure-fx) 0))
                  (.error js/console "API request error" error)
                  (mapv
                    #(rf/dispatch [% error])
                    failure-fx)))))))))
