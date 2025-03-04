(ns webclient.proxies.requests.panel
  (:require
    [re-frame.core :as rf]
    [clojure.string :as string]
    ["dayjs" :as dayjs]
    ["@mantine/core" :refer [Text Group Stack Divider Box Badge
                             Anchor MultiSelect TagsInput Button]]
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
        [:> Box {:p :md}
         [:> Stack {:gap :lg}
          [:header
           [h/page-title (:name selected-proxy)]]
          [:> Stack {:gap :md}
           [request-list-filters proxy-id]
           [requests-list
            (-> @requests :data :list)
            proxy-id]]]]))))
