(ns webclient.proxies.overview.panel
  (:require
    [re-frame.core :as rf]
    [reagent.core :as r]
    ["dayjs" :as dayjs]
    ["@mantine/core" :refer [Text Group Stack Box
                             SegmentedControl]]
    ["@mantine/charts" :refer [AreaChart]]
    [webclient.components.h :as h]))

(defmulti timeframe-dates identity)
(defmethod timeframe-dates "24 hours" [_]
  (let [today (.toISOString (.utc dayjs))
        last-24-hours (.toISOString (.utc dayjs (- (js/Date.now) 86400000)))]
    {:start last-24-hours :end today}))
(defmethod timeframe-dates "7 days" [_]
  (let [today (.toISOString (dayjs))
        seven-days-ago (.toISOString (dayjs (- (js/Date.now) 604800000)))]
    {:start seven-days-ago :end today}))
(defmethod timeframe-dates "30 days" [_]
  (let [today (.toISOString (dayjs))
        thirty-days-ago (.toISOString (dayjs (- (js/Date.now) 2592000000)))]
    {:start thirty-days-ago :end today}))

(defn- requests-chart [{:keys [proxy-id]}]
  (let [timeperiod-text (r/atom "24 hours")
        status-code-groups (r/atom #{:all :5 :4})
        last-24-hours-tf (timeframe-dates "24 hours")
        requests-timeframe (rf/subscribe [:requests->timeframe])
        fetch-timeframe (fn [t]
                          (let [tf-dates (timeframe-dates t)]
                            (reset! timeperiod-text t)
                            (doall (for [s @status-code-groups]
                              (rf/dispatch [:requests->get-timeframe-by-proxy
                                            {:proxy-id proxy-id
                                             :status-code-group s
                                             :start-time (:start tf-dates)
                                             :end-time (:end tf-dates)}])))))]

    (doall (for [s @status-code-groups]
      (rf/dispatch [:requests->get-timeframe-by-proxy
                    {:proxy-id proxy-id
                     :status-code-group s
                     :start-time (:start last-24-hours-tf)
                     :end-time (:end last-24-hours-tf)}])))

    (fn []
      (let [data (mapv (fn [all item-5 item-4]
                         {:date (:time_bucket all)
                          :all (:total all)
                          :5xx (:total item-5)
                          :4xx (:total item-4)})
                       (-> @requests-timeframe :data :all :list)
                       (-> @requests-timeframe :data :5xx :list)
                       (-> @requests-timeframe :data :4xx :list))]
        [:section
         [:> Group {:component :header
                    :align "start"
                    :gap :xl
                    :justify "space-between"}
          [:> Stack {:gap "0"
                     :style {:flex-grow 1}}
           [:> Text {:color "gray"}
            (str "Requests in the last " @timeperiod-text)]
           [:> Text {:fw 600
                     :size :xl
                     :color "gray.7"}
            (-> @requests-timeframe :data :all :total)]]
          [:> Box {:id "requests-chart-filter"
                   :style {:flexGrow 1}}
           [:> SegmentedControl {:size :xs
                                 :data ["24 hours" "7 days" "30 days"]
                                 :transitionDuration 0
                                 :withItemsBorders false
                                 :onChange #(fetch-timeframe %)
                                 :radius :xl
                                 :fullWidth true}]]]

         [:> AreaChart {:data (clj->js data)
                        :dataKey "date"
                        :curveType "monotone"
                        :yAxisProps {:orientation :right
                                     :tickMargin 20}
                        :gridAxis "none"
                        :withLegend true
                        :legendProps {:verticalAlign :bottom}
                        :dotProps {:r 0}
                        :tooltipAnimationDuration 200
                        :strokeWidth 1
                        :series [{:name "all" :color "gray.6"}
                                 {:name "5xx" :color "red.4"}
                                 {:name "4xx" :color "yellow.5"}]
                        :h 300}]]))))

(defn main [proxy-id]
  (let [proxies (rf/subscribe [:proxies])]
    (rf/dispatch [:requests->get-by-proxy-id proxy-id])
    (fn []
      (let [selected-proxy (->> (:data @proxies)
                                (filter #(= (:id %) proxy-id))
                                first)]
        [:> Box {:p :md}
         [:> Stack {:gap :lg}
          [:header
           [h/page-title (:name selected-proxy)]]
          [:> Stack {:gap :md}
           [requests-chart {:proxy-id proxy-id}]]]]))))
