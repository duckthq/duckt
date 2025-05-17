(ns webclient.sources.config.source-actions-list
  (:require
    [re-frame.core :as rf]
    [reagent.core :as r]
    [webclient.events.sources]
    ["@tabler/icons-react" :refer [IconCirclePlus IconRoute]]
    ["@mantine/core" :refer [Code Stack Highlight Text Title Button TextInput
                             Box Group Accordion Modal Select]]))

(defn action-http-config-form [_]
  (fn [action]
    [:> Stack {:gap :lg}
     [:> Stack
      [:> Title {:order 5}
       "HTTP Request"]
      [:> Text
       "Send a request to an endpoint"]]
     [:> Stack
      [:> Text
       "Request method"]
      [:> Select {:data ["GET" "POST" "PUT" "DELETE"]
                  :placeholder "Select one"
                  :label "Method"}]
      [:> Text
       "Request URL"]
      [:> TextInput {:placeholder "https://example.com"
                    :label "URL"}]]]))

(defn action-email-config-form [_]
  (fn [action]
    [:> Stack {:gap :lg}
     [:> Stack
      [:> Title {:order 5}
       "Email"]
      [:> Text
       "Send an email"]]
     [:> Stack
      [:> Text
       "Recipient email"]
      [:> TextInput {:placeholder ""}]]]))

;; empty state
(defn action-empty-config-form [_]
  [:> Stack {:gap :lg}
   [:> Stack
    [:> Title {:order 5}
     "Select an action type"]
    [:> Text
     "Select an action type to configure"]]])

(defn action-config []
  (fn [action-type]
    (case action-type
      "http-request" [action-http-config-form]
      "email" [action-email-config-form]
      [action-empty-config-form])))

(defn new-action-modal []
  (let [system-actions-types (rf/subscribe [:system/actions])
        selected-action-type (r/atom nil)]
    (rf/dispatch [:system->get-actions])
    (fn [opened {:keys [close]}]
      (println :system @selected-action-type)
      (let [action-types-otions (map #(into (sorted-map) {:label (:name %)
                                                          :value (:id %)})
                                     (:data @system-actions-types))]
        [:> Modal {:opened opened
                 :size :lg
                 :onClose #(close)
                 :title "New action"}
       [:> Stack {:gap :xl
                  :pt :sm}
        [:> Stack {:px :md}
         [:> Select {:label "Select an action type"
                     :placeholder "Select one"
                     :onChange #(reset! selected-action-type %)
                     :data action-types-otions}]
         [action-config @selected-action-type]]
        [:> Group {:gap 0
                   :grow 1}
         [:> Button {:variant :light}
          "Cancel"]
         [:> Button
          {:onClick #(println :create)}
          "Create"]]]]))))

(def payload-format
  (str
    "RequestPayload {\n"
    "  \"body\": \"{}\",\n"
    "  \"headers\": {\n"
    "      \"Authorization\": \"Bearer <token>\",\n"
    "  }\n"
    "}"))

(defn main []
  (let [new-action-modal-open? (r/atom true)]
    (fn [source source-actions]
      [:<>
       [new-action-modal
        @new-action-modal-open?
        {:close #(reset! new-action-modal-open? false)}]
       [:> Accordion {:multiple true
                      :defaultValue ["source-actions"]
                      :variant :separated}
        [:> Accordion.Item {:value "source-actions"}
         [:> Accordion.Control {:icon (r/as-element
                                        [:> IconRoute {:size 20}])}
          [:> Title {:order 5}
           (:name source)]]
         [:> Accordion.Panel
          [:> Stack {:p :md}
           [:> Stack
            [:> Text
             "Send requests to Duckt in this format:"]
            [:> Code {:block true
                      :styles {:root {:font-size "15px"}}}
             payload-format]
            [:> Highlight {:highlightStyles {:fontFamily "IBM Plex Mono"
                                             :fontWeight 800
                                             :backgroundColor "transparent"}
                           :highlight #js ["body", "headers"]}
             (str "The body and the headers of the request will be available "
                  "in the next action.")]]]]]

        (doall
          (for [action source-actions]
            ^{:key (:id action)}
            [:> Accordion.Item {:value (:id action)}
             [:> Accordion.Control
              [:> Text {:weight 500}
               (:name action)]]
             [:> Accordion.Panel
              [:> Stack {:p :md}
               [:> Stack
                [:> Title {:order 5}
                 "Send requests in this format:"]
                [:> Text {:color :dimmed}
                 "Quick add actions to your source"]]]]])) ]
       [:> Group {:align :center
                  :justify :center}
        [:> Button
         {:rightSection (r/as-element
                          [:> IconCirclePlus {:size 20
                                              :stroke 1.5}])
          :onClick #(reset! new-action-modal-open? true)}
         "New action"]]])))
