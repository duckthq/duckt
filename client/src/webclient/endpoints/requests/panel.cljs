(ns webclient.endpoints.requests.panel
  (:require
    [re-frame.core :as rf]
    ["@radix-ui/themes" :refer [Badge Box Table Text
                                Tooltip]]
    ["dayjs" :as dayjs]
    ["dayjs/plugin/relativeTime" :as relative-time]
    [webclient.components.h :as h]
    [webclient.routes :as routes]))

(.extend dayjs relative-time)

(def method-colors
  {"GET" "green"
   "POST" "blue"
   "PUT" "yellow"
   "DELETE" "tomato"})

(defn main [endpoint-id]
  (let [requests (rf/subscribe [:endpoints->requests-list])
        endpoint (rf/subscribe [:endpoints->endpoint])
        host (rf/subscribe [:hosts->host])]
    (rf/dispatch [:endpoints->get-requests endpoint-id])
    (rf/dispatch [:endpoints->get-endpoint endpoint-id])
    (rf/dispatch [:hosts->get-host-by-endpoint-id endpoint-id])
    (fn []
      (println :endpoint @endpoint)
      [:div
       [:header {:class "mb-4"}
        [h/h1 {:text (str (-> @endpoint :data :method)
                          " Requests for "
                          (-> @endpoint :data :path))}]]
       [:> Box
        [:> Table.Root  {:variant "surface"}
         [:> Table.Header
          [:> Table.Row
           [:> Table.ColumnHeaderCell "Status"]
           [:> Table.ColumnHeaderCell "Requested at"]
           [:> Table.ColumnHeaderCell "Time (ms)"]]]
         [:> Table.Body
          (for [request (-> @requests :data :list)]
            [:> Table.Row {:key (:id request)}
             [:> Table.RowHeaderCell (:status_code request)]
             [:> Table.Cell [:> Tooltip {:content (:created_at request)}
                             [:> Text {:size "1"
                                       :style {:textDecoration "underline"}}
                              (-> (dayjs (:created_at request))
                                  (.fromNow))]]]
             [:> Table.Cell (str (:elapsed_time request)
                                 " ms")]])]]]])))
