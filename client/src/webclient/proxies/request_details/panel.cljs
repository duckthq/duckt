(ns webclient.proxies.request-details.panel
  (:require
    [re-frame.core :as rf]
    [reagent.core :as r]
    ["@mantine/core" :refer [Stack Group Badge Table Paper Box]]
    [webclient.components.ui.title :as title]
    [webclient.components.ui.text :as text]))

(defn main [_ request-id]
  (let [request (rf/subscribe [:requests->request-details])]
    (rf/dispatch [:requests->get-request-details request-id])
    (fn []
      (js/console.log "request" (clj->js @request))
      [:<>
       [:> Stack {:p :md}
        [title/page-title "Request Details"]
        (if (:loading? @request)
          [:div :loading]
          [:> Stack
           [:> Group {:grow true}
            [:> Box
             [:> Table {:variant :vertical}
              [:> Table.Tbody
               [:> Table.Tr
                [:> Table.Th "Method"]
                [:> Table.Td
                 [:> Badge {:color :teal}
                  (-> @request :data :method)]]]
               [:> Table.Tr
                [:> Table.Th "Status Code"]
                [:> Table.Td
                 [text/Base (-> @request :data :status_code)]]]
               [:> Table.Tr
                [:> Table.Th "Elapsed Time"]
                [:> Table.Td
                 [text/Base (str (-> @request :data :status_code) " ms")]]]
               [:> Table.Tr
                [:> Table.Th "Request created time"]
                [:> Table.Td
                 [text/Base (-> @request :data :created_at)]]]
               [:> Table.Tr
                [:> Table.Th "Response time"]
                [:> Table.Td
                 [text/Base (-> @request :data :response_time)]]]
               ]]]
            [:> Paper {:withBorder true
                       :p :sm}
             "Information"]]])]])))
