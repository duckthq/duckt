(ns webclient.proxies.settings.panel
  (:require
    [re-frame.core :as rf]
    [reagent.core :as r]
    ["@mantine/core" :refer [Paper Stack Group Divider Tooltip Button Radio Text]]
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

(defn- swap-key-confirmation [proxy-id]
  [:> Stack
   [:> Stack {:gap :xs}
    [title/h5 "Are you sure you want to swap the key?"]
    [text/Dimmed {:size :md}
     "This action cannot be undone. This will invalidate the current key and generate a new one."]]
   [:> Group {:justify :end}
    [button/Secondary {:onClick #(rf/dispatch [:modal->close])}
     "Cancel"]
    [:> Button {:color :red
                :variant :outline
                :onClick #(rf/dispatch [:proxies->swap-key proxy-id])
                :size :md}
     "Swap"]]])

(defn- proxy-keys [proxy-info]
  (declare swapped-key)
  (r/with-let [swapped-key (rf/subscribe [:proxies->swapped-key])]
    [:> Stack
     [title/h3 "Authentication"]
     [:> Group
      [:> Paper {:p :md
                 :withBorder true}
       [:> Group
        [text/Base {:fw 600} "Proxy key"]
        [text/Dimmed {:size :md}
         (if @swapped-key
           @swapped-key
           (take 80 (repeat "•")))]
        [:> Group
         [button/Primary
          {:size :sm
           :onClick #(rf/dispatch [:modal->open {:content [swap-key-confirmation
                                                           (:id proxy-info)]
                                                 :title "Swap Key"}])}
          "Swap key"]
         [:> Tooltip {:label (str "This key is never stored in plain text anywhere. "
                                  "Swaping it will invalidate the current key and generate a new one. ")
                      :arrowOffset 10
                      :offset 10
                      :withArrow true
                      :color :dark
                      :multiline true
                      :w 200}
          [:> IconInfoCircle {:size 20 :stroke 1.5}]]]]]]]
    (finally (rf/dispatch [:proxies->clean-swapped-key-from-memory]))))

(defn- networking-info [info]
  (let [proxy-info-target-url (r/atom (:target_url info))]
    (fn [info]
      [:> Stack {:maw 700
                 :pb :xl}
       [title/h3 "Networking configuration"]
       [:> Stack
        [forms/input-field
         {:label "Target URL"
          :onChange #(reset! proxy-info-target-url (-> % .-target .-value))
          :defaultValue @proxy-info-target-url}]
        [:> Radio.Group
         {:value "partial"
          :onChange #(println %)}
         [:> Stack {:gap :xs}
          [:> Radio.Card
           {:value "partial"
            :p :sm}
           [:> Group {:align :flex-start}
            [:> Radio.Indicator]
            [:> Stack {:gap :xs}
             [:> Text "Partial"]
             [:> Text {:size :md
                       :color :dimmed}
              "Proxy only requests that match the target URL will be forwarded."]]]]
          [:> Radio.Card
           {:value "full"}
           [:> Stack
            [text/Base {:fw 600} "Full"]
            [text/Dimmed {:size :md}
             "All requests will be forwarded to the target URL."]]]]]

        [:> Group {:align :end
                   :justify :end}
         [button/primary
          {:text "Save"
           :on-click #(rf/dispatch [:proxies->update
                                    (:id info)
                                    {:target-url @proxy-info-target-url}])}]]]])))


(defn- basic-info [info]
  (let [proxy-info-name (r/atom (:name info))
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
        [:> Group {:align :end
                   :justify :end}
         [button/primary
          {:text "Save"
           :on-click #(rf/dispatch [:proxies->update
                                    (:id info)
                                    {:name @proxy-info-name
                                     :description @proxy-info-description}])}]]]])))

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
          [networking-info (:data @proxy-info)]
          [:> Divider]
          [proxy-keys (:data @proxy-info)]
          [:> Divider]
          [danger-zone proxy-id]]]))))
