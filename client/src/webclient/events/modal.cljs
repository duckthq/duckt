(ns webclient.events.modal
  (:require
    [re-frame.core :as rf]))

(rf/reg-event-fx
  :modal->open
  (fn [{:keys [db]} [_ {:keys [content id title props]}]]
    {:db (assoc db :modals (conj (:modals db) {:id id
                                               :title title
                                               :content content
                                               :props props}))}))

(rf/reg-event-fx
  :modal->close
  (fn [{:keys [db]} [_]]
    (let [updated-modals (pop (:modals db))]
      {:db (assoc db :modals updated-modals)})))

(rf/reg-sub
  :modals
  (fn [db]
    (:modals db)))
