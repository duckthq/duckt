(ns webclient.proxies.settings.panel
  (:require
    [re-frame.core :as rf]
    [reagent.core :as r]
    ["@mantine/core" :refer [Paper Stack Group Divider Tooltip Button]]
    ["@tabler/icons-react" :refer [IconInfoCircle]]
    [webclient.components.ui.title :as title]
    [webclient.components.forms :as forms]
    [webclient.components.button :as button]
    [webclient.components.ui.text :as text]))

(defn- danger-zone [proxy-id]
  [:> Stack {:pb :xl}
   [title/h3 "Danger Zone"]
   [:> Stack {:gap :xs}
    [title/h5 "Delete Proxy"]
    [text/Dimmed {:size :md}
     "This action cannot be undone. This will permanently delete the proxy and all its data."]]
   [:> Group
    [:> Button {:color :red
                :variant :outline
                :onClick #(rf/dispatch [:proxies->delete proxy-id])
                :size :md}
     "Delete"]]])

(defn- proxy-keys []
  [:> Stack
   [title/h3 "Authentication"]
   [:> Group
    [:> Paper {:p :md
               :withBorder true}
     [:> Group
      [text/Base {:fw 600} "Proxy key"]
      [text/Dimmed {:size :md}
       (take 80 (repeat "•"))]
      [:> Group
       [button/Primary
        {:size :sm}
        "Swap key"]
       [:> Tooltip {:label (str "This key is never stored in plain text anywhere. "
                                "Swaping it will invalidate the current key and generate a new one. ")
                    :arrowOffset 10
                    :offset 10
                    :withArrow true
                    :color :dark
                    :multiline true
                    :w 200}
        [:> IconInfoCircle {:size 20 :stroke 1.5}]]]]]]])

(defn- basic-info [info]
  (let [proxy-info-name (r/atom (:name info))
        proxy-info-target-url (r/atom (:target_url info))
        proxy-info-host-url (r/atom (:host_url info))
        proxy-info-description (r/atom (:description info))]
  (fn [info]
    [:> Stack {:maw 700
               :pb :xl}
     [title/h3 "Basic Information"]
     [:> Stack
      [forms/input-field
       {:label "Name"
        :onChange #(reset! proxy-info-name (-> % .-target .-value))
        :defaultValue @proxy-info-name}]
      [forms/textarea-field
       {:label "Description"
        :placeholder "(Optional) Describe this Proxy purpose"
        :onChange #(reset! proxy-info-description (-> % .-target .-value))
        :defaultValue @proxy-info-description}]
      [forms/input-field
       {:label "Target URL"
        :onChange #(reset! proxy-info-target-url (-> % .-target .-value))
        :defaultValue @proxy-info-target-url}]
      [forms/input-field
       {:label "Host URL"
        :onChange #(reset! proxy-info-host-url (-> % .-target .-value))
        :defaultValue @proxy-info-host-url}]
      [:> Group {:align :end
                 :justify :end}
       [button/primary
        {:text "Save"
         :on-click #(rf/dispatch [:proxies->update
                                  (:id info)
                                  {:name @proxy-info-name
                                   :description @proxy-info-description
                                   :host-url @proxy-info-host-url
                                   :target-url @proxy-info-target-url}])}]]]])))

(defn main [proxy-id]
  (let [proxy-info (rf/subscribe [:proxies->proxy-info])]
    (rf/dispatch [:proxies->get-by-id proxy-id])
    (fn []
      (if (:loading? @proxy-info)
        [:div :loading]
        [:> Stack {:p :md}
         [title/page-title
          "Proxy Settings"]
         [:> Stack
          [basic-info (:data @proxy-info)]
          [:> Divider]
          [proxy-keys (:data @proxy-info)]
          [:> Divider]
          [danger-zone proxy-id]]]))))
