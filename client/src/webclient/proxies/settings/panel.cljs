(ns webclient.proxies.settings.panel
  (:require
    [re-frame.core :as rf]
    [reagent.core :as r]
    ["@mantine/core" :refer [Paper Stack Group Divider Tooltip Button Radio Text
                             TextInput TagsInput Button]]
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
  (let [proxy-info-target-url (r/atom (:target_url info))
        capture-type-radio-values [{:label "Partial" :value "partial"
                                    :description (str "Capture only the default Duckt headers and "
                                                      "the fields you specify in the configuration.")}
                                   {:label "Full" :value "full"
                                    :description (str "Capture all headers and fields sent by the client. "
                                                      "This includes sensitive information like passwords.")}
                                   {:label "None" :value "none"
                                    :description (str "Do not capture any data. "
                                                      "This is useful for testing the proxy without capturing any data.")}]
        response-capture-type-value (r/atom (or (-> info :response_headers_config :type) "partial"))
        request-capture-type-value (r/atom (or (-> info :request_headers_config :type) "partial"))
        request-headers-items (r/atom (or (-> info :request_headers_config :keys) []))
        response-headers-items (r/atom (or (-> info :response_headers_config :keys) []))]
    (fn [info]
      (let [save (fn []
                   (rf/dispatch [:proxies->update
                                 (:id info)
                                 {:target-url @proxy-info-target-url
                                  :request-headers-config {:type @request-capture-type-value
                                                           :keys @request-headers-items}
                                  :response-headers-config {:type @response-capture-type-value
                                                            :keys @response-headers-items}}]))]
        [:> Stack {:maw 700
                   :pb :xl}
         [title/h3 "Networking configuration"]
         [:> Stack {:gap :xl}
          [:> TextInput
           {:label "Target URL"
            :description "The URL of the server that will receive the requests"
            :onChange #(reset! proxy-info-target-url (-> % .-target .-value))
            :defaultValue @proxy-info-target-url}]

          [:> Stack
           [title/h4 "Request headers"]
           [:> Radio.Group
            {:value @request-capture-type-value
             :label "Capture type"
             :description (str "Choose how you want to capture the request headers "
                               "of each request")
             :onChange #(reset! request-capture-type-value %)}
            [:> Group {:wrap "nowrap"
                       :pt :md
                       :align :start
                       :justify "stretch"
                       :grow 1}
             (doall
               (for [capture-type capture-type-radio-values]
                 ^{:key (:value capture-type)}
                 [:> Radio.Card
                  {:value (:value capture-type)
                   :withBorder false
                   :h "100%"
                   :checked (= @request-capture-type-value (:value capture-type))
                   :p :sm}
                  [:> Stack {:gap :xs}
                   [:> Group {:wrap :nowrap}
                    [:> Radio.Indicator]
                    [:> Text (:label capture-type)]]
                   [:> Text {:size :sm
                             :color :dimmed}
                    (:description capture-type)]]]))]]
           [:> TagsInput {:label "Request headers"
                          :description "The headers you want to capture from the request"
                          :disabled (not= @request-capture-type-value "partial")
                          :value (if (= @request-capture-type-value "partial")
                                   @request-headers-items
                                   [])
                          :onChange #(reset! request-headers-items %)
                          :placeholder "Enter header key"}]]

          [:> Stack
           [title/h4 "Response headers"]
           [:> Radio.Group
            {:value @response-capture-type-value
             :label "Capture type"
             :description (str "Choose how you want to capture the response headers "
                               "of each request")
             :onChange #(reset! response-capture-type-value %)}
            [:> Group {:wrap "nowrap"
                       :pt :md
                       :align :start
                       :justify "stretch"
                       :grow 1}
             (doall
               (for [capture-type capture-type-radio-values]
                 ^{:key (:value capture-type)}
                 [:> Radio.Card
                  {:value (:value capture-type)
                   :withBorder false
                   :h "100%"
                   :checked (= @response-capture-type-value (:value capture-type))
                   :p :sm}
                  [:> Stack {:gap :xs}
                   [:> Group {:wrap :nowrap}
                    [:> Radio.Indicator]
                    [:> Text (:label capture-type)]]
                   [:> Text {:size :sm
                             :color :dimmed}
                    (:description capture-type)]]]))]]
           [:> TagsInput {:label "Response headers"
                          :description "The headers you want to capture from the response"
                          :disabled (not= @response-capture-type-value "partial")
                          :value (if (= @response-capture-type-value "partial")
                                   @response-headers-items
                                   [])
                          :onChange #(reset! response-headers-items %)
                          :placeholder "Enter header key"}]]

          [:> Group {:align :end
                     :justify :end}
           [:> Button
            {:onClick save}
            "Save"]]]]))))


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
