(ns webclient.proxies.request-details.panel
  (:require
    [re-frame.core :as rf]
    [reagent.core :as r]
    ["@mantine/core" :refer [Stack Group Badge Table Paper Box
                             Code]]
    ["@tabler/icons-react" :refer [IconArrowBack]]
    [webclient.components.ui.title :as title]
    [webclient.components.ui.anchor :as anchor]
    [webclient.components.ui.text :as text]
    [webclient.components.button :as button]
    [webclient.routes :as routes]))

(defn- basic-info-table [request]
  [:> Table {:variant :vertical}
   [:> Table.Tbody
    [:> Table.Tr
     [:> Table.Th "URI"]
     [:> Table.Td
       [:> Code(-> request :uri)]]]
    [:> Table.Tr
     [:> Table.Th "Method"]
     [:> Table.Td
      [:> Badge {:color :teal}
       (-> request :method)]]]
    [:> Table.Tr
     [:> Table.Th "Status Code"]
     [:> Table.Td
      [text/Base (-> request :status_code)]]]
    [:> Table.Tr
     [:> Table.Th "Elapsed Time"]
     [:> Table.Td
      [text/Base (str (-> request :elapsed_time) " ms")]]]
    [:> Table.Tr
     [:> Table.Th "Request created time"]
     [:> Table.Td
      [text/Base (-> request :created_at)]]]
    [:> Table.Tr
     [:> Table.Th "Response time"]
     [:> Table.Td
      [text/Base (-> request :response_time)]]]]])

(defn- query-params-table []
  (fn [query-params]
    [:> Table {:variant :vertical
               :withTableBorder true
               :verticalSpacing :sm
               :horizontalSpacing :md
               :highlightOnHover true}
     [:> Table.Tbody
      (doall
        (for [q query-params]
          ^{:key (first q)}
          [:> Table.Tr
           [:> Table.Th
            (first q)]
           [:> Table.Td
            [text/Base (second q)]]]))]]))

(defn- headers-table []
  (fn [headers]
    [:> Table {:variant :vertical
               :withTableBorder true
               :verticalSpacing :sm
               :horizontalSpacing :md
               :highlightOnHover true}
     [:> Table.Tbody
      (doall
        (for [h headers]
          ^{:key (first h)}
          [:> Table.Tr
           [:> Table.Th
            (first h)]
           [:> Table.Td
            [text/Base (second h)]]]))]]))

(defn main [proxy-id request-id]
  (let [request (rf/subscribe [:requests->request-details])]
    (rf/dispatch [:requests->get-request-details request-id])
    (fn []
      [:<>
       [:> Stack {:p :md}
        [:> Group
         [anchor/Dark {:href (routes/url-for :proxy-requests {:proxy-id proxy-id})}
          [:> Group {:gap :xs}
           [:> IconArrowBack {:size 20
                              :stroke 1.5}]
           [text/Base "Back to requests"]]]
         [:> Badge {:color :gray
                    :size :md
                    :style {:marginLeft "auto"}}
          "Request ID: " request-id]]
        [title/page-title
         "Request Details"
         "View details of the request"]
        (if (:loading? @request)
          [:div :loading]
          [:> Stack {:gap :xl}
           [:> Group {:gap :xl
                      :align :start}
            [:> Stack
             [title/h3 "Overview"]
             [basic-info-table (:data @request)]]
            (comment [:> Stack {:grow true}
             [title/h3 "User information"]
             [:> Box {:pos :relative}
              "WIP"]])]
           [:> Stack
            [title/h3 "Query parameters"]
            [query-params-table (-> @request :data :query_params)]]
           [:> Stack
            [title/h3 "Request headers"]
            [headers-table (-> @request :data :request_headers)]]
           [:> Stack
            [title/h3 "Response headers"]
            [headers-table (-> @request :data :response_headers)]]
           ])]])))
