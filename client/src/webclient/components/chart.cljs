(ns webclient.components.chart
  (:require ["chart.js/auto" :as Chart]
            [re-frame.core :as re-frame]))

(defn main [{:keys [config id]}]
  (let [container-ref (atom nil)]
    (fn []
      [:canvas {:id id
                :ref (fn [el]
                       (when el
                         (new Chart
                              (.getElementById js/document (.-id el))
                              (clj->js config))
                         (reset! container-ref el)))}])))
