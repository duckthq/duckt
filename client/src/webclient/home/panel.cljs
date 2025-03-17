(ns webclient.home.panel
  (:require
    [reagent.core :as r]
    ["dayjs" :as dayjs]
    ["@mantine/core" :refer [Text Group Stack Box Grid Paper
                             SegmentedControl Divider Avatar]]
    ["@mantine/charts" :refer [AreaChart BarChart]]
    [webclient.components.h :as h]
    [webclient.routes :as routes]
    [re-frame.core :as rf]))

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

(defn- requests-chart []
  (let [ timeperiod-text (r/atom "24 hours")
        status-code-groups (r/atom #{:all :5 :4})
        last-24-hours-tf (timeframe-dates "24 hours")
        requests-timeframe (rf/subscribe [:requests->timeframe])
        fetch-timeframe (fn [t]
                          (let [tf-dates (timeframe-dates t)]
                            (reset! timeperiod-text t)
                            (doall (for [s @status-code-groups]
                              (rf/dispatch [:requests->get-timeframes
                                            {:status-code-group s
                                             :start-time (:start tf-dates)
                                             :end-time (:end tf-dates)}])))))]

    (doall (for [s @status-code-groups]
      (rf/dispatch [:requests->get-timeframes
                    {:status-code-group s
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
                                 :fullWidth true}]]]

         [:> BarChart {:data (clj->js data)
                        :dataKey "date"
                        :yAxisProps {:orientation :right
                                     :tickMargin 20}
                        :gridAxis "none"
                        :withLegend true
                        :legendProps {:verticalAlign :bottom}
                        :barProps {:radius 10}
                        :tooltipAnimationDuration 200
                        :strokeWidth 1
                        :series [{:name "all" :color "gray.6"}
                                 {:name "5xx" :color "red.4"}
                                 {:name "4xx" :color "yellow.5"}]
                        :h 180}]]))))

(defn customer-list-item [customer]
  [:> Box
   [:> Group {:p :sm
              :gap :xs}
    [:> Avatar {:color "initials"
                :variant :filled
                :size :xs
                :name (or (:sub customer) "A")}]
    [:> Text {:size :xs}
     (:sub customer)]
    [:> Text {:size :xs
              :align :right
              :style {:flexGrow 1}
              :c :dimmed}
     (str
       "Seen "
       (:hit_count customer)
       " times")]]
   [:> Divider]])

(defn customers-list [customers]
  [:> Stack
   [:> Text {:size :md
             :c :dimmed}
    "Most active users"]
   (for [customer (:list customers)]
     ^{:key (:id customer)}
     [customer-list-item customer])])

(defn panel []
  (let [customers (rf/subscribe [:customers->most-active])]
    (rf/dispatch [:customers->get-most-active])
    (fn []
      [:> Box {:p :md}
       [:> Stack {:gap :lg}
        [:header
         [h/page-title "Home"]]
        [:> Group {:gap :lg
                   :align :stretch
                   :grow 1}
         [:> Paper {:withBorder true
                    :p :lg}
          [requests-chart]]
         [:> Paper {:withBorder true
                    :p :lg}
          [customers-list (:data @customers)]]]]])))
