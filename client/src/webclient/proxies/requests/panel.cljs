(ns webclient.proxies.requests.panel
  (:require
    [re-frame.core :as rf]
    [clojure.string :as string]
    ["dayjs" :as dayjs]
    ["@mantine/core" :refer [Text Group Stack Divider Box Badge
                             Anchor MultiSelect TagsInput]]
    [webclient.components.h :as h]
    [webclient.routes :as routes]
    [bidi.bidi :as bidi]))

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
  [:section
   [:> Stack {:gap :xs}
    (for [request requests]
      ^{:key (:id request)}
      [request-list-item request proxy-id])]])

(defn- request-list-filters [proxy-id]
  (let [requests (rf/subscribe [:requests])]
    (fn []
      (let [filters (-> @requests :filters)
            methods-value (when-not (string/blank? (get filters "methods"))
                            (string/split (get filters "methods") #","))
            status-codes-value (when-not (string/blank? (get filters "status_codes"))
                                (string/split (get filters "status_codes") #","))]
        [:> Group
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

(defn main []
  (let [requests (rf/subscribe [:requests])
        proxies (rf/subscribe [:proxies])]
    ;; for direct link rendering
    (rf/dispatch [:requests->get-by-proxy-id (routes/get-page-param :proxy-id)])
    (fn []
      (let [proxy-id (routes/get-page-param :proxy-id)
            selected-proxy (->> (:data @proxies)
                                (filter #(= (:id %) proxy-id))
                                first)]
        ;(println :selected-proxy selected-proxy)
        (println :proxy-id proxy-id)
        (when-not (= (:id selected-proxy) proxy-id)
          (println :hue selected-proxy proxy-id))
        [:> Box {:p :md}
         [:> Stack {:gap :lg}
          [:header
           [h/page-title (:name selected-proxy)]]
          [:> Stack {:gap :md}
           [request-list-filters proxy-id]
           [requests-list
            (-> @requests :data :list)
            proxy-id]]]]))))
