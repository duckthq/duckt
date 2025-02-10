(ns webclient.proxies.settings.panel
  (:require
    [re-frame.core :as rf]
    ["@mantine/core" :refer [Stack Box Divider]]
    [webclient.components.ui.title :as title]
    [webclient.components.forms :as forms]))

(defn- proxy-keys []
  [:> Stack
   [title/h3 "Authentication"]])

(defn- basic-info []
  (fn [info]
    (println :info info)
    [:> Stack {:maw 700
               :pb :xl}
     [title/h3 "Basic Information"]
     [:> Stack
      [forms/input-field {:label "Name"
                          :defaultValue (:name info)}]
      [forms/textarea-field {:label "Description"
                             :placeholder "(Optional) Describe this Proxy purpose"
                             :defaultValue (:description info)}]]]))

(defn main [proxy-id]
  (let [proxy-info (rf/subscribe [:proxies->proxy-info])]
    (rf/dispatch [:proxies->get-by-id proxy-id])
    (fn []
      (js/console.log "proxy-info" (clj->js @proxy-info))
      (if (:loading? @proxy-info)
        [:div :loading]
        [:> Stack {:p :md}
         [title/page-title
          "Proxy Settings"]
         [:> Stack
          [basic-info (:data @proxy-info)]
          [:> Divider]
          [proxy-keys (:data @proxy-info)]]]))))
