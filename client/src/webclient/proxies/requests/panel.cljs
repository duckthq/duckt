(ns webclient.proxies.requests.panel
  (:require
    [re-frame.core :as rf]
    [reagent.core :as r]
    [clojure.string :as string]
    ["dayjs" :as dayjs]
    ["@mantine/core" :refer [Text Group Stack Divider Box Badge SegmentedControl
                             Anchor MultiSelect TagsInput Button]]
    ["@mantine/charts" :refer [BarChart]]
    [webclient.components.h :as h]
    [webclient.routes :as routes]))

(defn- request-list-item [request proxy-id]
  [:> Anchor {:href (routes/url-for :request-details
                                    {:proxy-id proxy-id
                                     :request-id (:id request)})}
   [:> Group {:p :md}
    [:> Badge {:size :md}
     (:method request)]
    [:> Text {:size :xs}
     (:status_code request)]
    [:> Divider {:orientation :vertical}]
    [:> Text {:size :sm}
     (:uri request)]
    [:> Text {:size :xs
              :color :gray}
     (:created_at request)]
    [:> Text {:size :xs
              :style {:flexGrow 1}
              :align :right
              :color :gray}
     (str (:elapsed_time request) " ms")]]
   [:> Divider {:variant :dashed}]])

(defn- requests-list [requests proxy-id]
  [:> Stack {:gap :xl}
   [:> Stack {:gap :xs}
    (for [request requests]
      ^{:key (:id request)}
      [request-list-item request proxy-id])]
   [:> Group {:justify :center}
    [:> Button
     {:variant :default
      :onClick #(rf/dispatch [:requests->load-more proxy-id])}
     "Load more"]]])

(defn- request-list-filters [proxy-id]
  (let [requests (rf/subscribe [:requests])
        customers (rf/subscribe [:customers])]
    (rf/dispatch [:customers->get])
    (fn []
      (let [filters (-> @requests :filters)
            methods-value (when-not (string/blank? (get filters "methods"))
                            (string/split (get filters "methods") #","))
            customers-value (when-not (string/blank? (get filters "customers_subs"))
                              (string/split (get filters "customers_subs") #","))
            status-codes-value (when-not (string/blank? (get filters "status_codes"))
                                (string/split (get filters "status_codes") #","))]
        [:> Group
         [:> MultiSelect {:label "Customers"
                          :onChange #(rf/dispatch [:requests->filter {"customers_subs" %} proxy-id])
                          :clearable true
                          :searchable true
                          :value (or customers-value [])
                          :nothingFoundMessage "Nothing found..."
                          :checkIconPosition :right
                          :data (or (map :sub (-> @customers :data :list)) [])
                          :placeholder "Select methods"}]
         [:> MultiSelect {:label "Methods"
                          :onChange #(rf/dispatch [:requests->filter {"methods" %} proxy-id])
                          :clearable true
                          :searchable true
                          :value (or methods-value [])
                          :nothingFoundMessage "Nothing found..."
                          :checkIconPosition :right
                          :data ["GET" "POST" "PUT" "PATCH" "CONNECT"
                                 "HEAD" "DELETE" "OPTIONS" "TRACE"]
                          :placeholder "Select methods"}]
         [:> TagsInput {:label "Status code"
                        :onChange #(rf/dispatch [:requests->filter {"status_codes" %} proxy-id])
                        :clearable true
                        :value (or status-codes-value [])
                        :data []
                        :placeholder "Select methods"}]]))))

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

(defn- requests-chart [proxy-id]
  (let [cached-proxy-id (r/atom proxy-id)
        timeperiod-text (r/atom "24 hours")
        status-code-groups (r/atom #{:all :5 :4})
        last-24-hours-tf (timeframe-dates "24 hours")
        requests-timeframe (rf/subscribe [:requests->timeframe])
        fetch-timeframe (fn [t]
                          (let [tf-dates (timeframe-dates t)]
                            (reset! timeperiod-text t)
                            (doall (for [s @status-code-groups]
                              (rf/dispatch [:requests->get-timeframe-by-proxy
                                            {:proxy-id (routes/get-page-param :proxy-id)
                                             :status-code-group s
                                             :start-time (:start tf-dates)
                                             :end-time (:end tf-dates)}])))))]

    (doall (for [s @status-code-groups]
      (rf/dispatch [:requests->get-timeframe-by-proxy
                    {:proxy-id proxy-id
                     :status-code-group s
                     :start-time (:start last-24-hours-tf)
                     :end-time (:end last-24-hours-tf)}])))

    (fn [local-proxy-id]
      ;; logic for when the user moves from one proxy overview to another
      ;; and the page params will stay the same and won't get the info
      ;; from the right proxy
      (when-not (= @cached-proxy-id local-proxy-id)
        (reset! cached-proxy-id local-proxy-id)
        (doall (for [s @status-code-groups]
                 (rf/dispatch [:requests->get-timeframe-by-proxy
                               {:proxy-id local-proxy-id
                                :status-code-group s
                                :start-time (:start last-24-hours-tf)
                                :end-time (:end last-24-hours-tf)}]))))
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
                        :h 200}]]))))

(defn main []
  (let [requests (rf/subscribe [:requests])
        active-proxy (rf/subscribe [:proxies->active-proxy])
        proxies (rf/subscribe [:proxies])]
    ;; for direct link rendering
    (rf/dispatch [:requests->get-by-proxy-id (routes/get-page-param :proxy-id)])
    (rf/dispatch [:proxies->set-active-proxy (routes/get-page-param :proxy-id)])
    (fn []
      (let [proxy-id (routes/get-page-param :proxy-id)
            selected-proxy (->> (:data @proxies)
                                (filter #(= (:id %) proxy-id))
                                first)]
        [:> Box {:p :md}
         [:> Stack {:gap :lg}
          [:header
           [h/page-title (:name selected-proxy)]]
          [:> Stack
           [requests-chart (or @active-proxy proxy-id)]]
          [:> Stack {:gap :md}
           [request-list-filters proxy-id]
           [requests-list
            (-> @requests :data :list)
            proxy-id]]]]))))
