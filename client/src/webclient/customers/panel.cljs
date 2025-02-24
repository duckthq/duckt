(ns webclient.customers.panel
  (:require
    [re-frame.core :as rf]
    ["@mantine/core" :refer [Stack Group Box Divider Avatar]]
    [webclient.components.ui.text :as text]
    [webclient.components.ui.title :as title]))

(defn- customer-list-item [customer proxy-id]
  [:> Box
   [:> Group {:p :md}
    [:> Avatar {:color "initials"
                :variant :filled
                :size :sm
                :name (:sub customer)}]
    [text/Base {:size :md}
     (:sub customer)]
    [text/Base {:size :xs}
     (:last_seen_at customer)]
    [:> Divider {:orientation :vertical}]
    [text/Base {:size :xs
              :color :gray}
     (:hit_count customer)]]
   [:> Divider {:variant :dashed}]])


(defn customers-list [customers proxy-id]
  [:> Stack
   (for [customer (:list customers)]
     ^{:key (:id customer)}
     [customer-list-item customer])])

(defn customers-filters [proxy-id]
  [:> Group
   ])

(defn main [proxy-id]
  (let [customers (rf/subscribe [:customers])]
    (rf/dispatch [:customers->get])
    (fn []
      (js/console.log "customers" (clj->js @customers))
      [:> Stack {:p :md}
       [title/page-title "Customers"]
       [:> Stack
        [customers-filters proxy-id]
        [customers-list (:data @customers) proxy-id]]])))
