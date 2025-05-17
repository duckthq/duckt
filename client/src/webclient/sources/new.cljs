(ns webclient.sources.new
  (:require
    [reagent.core :as r]
    [re-frame.core :as rf]
    ["@mantine/core" :refer [Stack Group Grid Text Box TextInput Button Title]]))

(defn panel []
  (let [name (r/atom nil)
        description (r/atom nil)]
    (fn []
      [:> Stack {:gap :xl
                 :p "md"
                 :h "100%"}
       [:> Stack {:gap 0
                  :grow 1
                  :style {:flexGrow 1}}
        [:> Title {:order 1}
         "New source"]
        [:> Text {:color :dimmed}
         "Create a new source to super power your data."]]
       [:form {:id "new-proxy-form"
               :style {:height "100%"
                       :flexGrow 1}
               :on-submit #(do (.preventDefault %)
                               (js/console.log "submit")
                               (rf/dispatch [:sources->create
                                             {:name @name
                                              :description @description}]))}
        [:section {:id "new-sources-content"}
         [:> Grid
          [:> Grid.Col {:span {:base 12 :sm 6}}
           [:> Stack {:gap :lg}
            [:> TextInput {:placeholder "Source name"
                           :name "source-name"
                           :label "Name"
                           :onChange #(reset! name (.. % -target -value))
                           :description "Identify your source by a name of your choice"
                           :required true}]
            [:> TextInput {:placeholder "Description"
                           :name "description"
                           :onChange #(reset! description (.. % -target -value))
                           :label "Description"
                           :description "(optional) Describe the purpose or a description for this proxy"}]]]]]
        [:> Box {:pos "absolute"
                 :w "100%"
                 :style {:borderTop "1px solid var(--mantine-color-gray-2)"}
                 :bottom 0
                 :right 0}
         [:> Group {:gap :md
                    :align :center
                    :justify "end"}
          [:> Text {:color :gray
                    :size :sm}
           "You can edit it later if you need"]
          [:> Group {:gap 0}
           [:> Button {:type "button"
                       :variant :light}
            "Cancel"]
           [:> Button {:type "submit"}
            "Create source"]]]]]])))
