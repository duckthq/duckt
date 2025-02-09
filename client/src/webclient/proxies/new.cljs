(ns webclient.proxies.new
  (:require
    [reagent.core :as r]
    [re-frame.core :as rf]
    ["@mantine/core" :refer [Stack Group Grid Text Box]]
    [webclient.components.forms :as forms]
    [webclient.components.button :as button]
    [webclient.components.h :as h]))

(defn panel []
  (let [name (r/atom nil)
        target-url (r/atom nil)
        proxy-host (r/atom nil)
        description (r/atom nil)]
    (fn []
      [:> Stack {:gap :xl
                 :p "md"
                 :h "100%"}
       [:> Stack {:gap 0
                  :grow 1
                  :style {:flexGrow 1}}
        [h/page-title "New proxy"]
        [:> Text {:color "gray"}
         "Create a new proxy to access and control your data."]]
       [:form {:id "new-proxy-form"
               :style {:height "100%"
                       :flexGrow 1}
               :on-submit #(do (.preventDefault %)
                               (js/console.log "submit")
                               (rf/dispatch [:proxies->create
                                             {:name @name
                                              :target-url @target-url
                                              :host-url @proxy-host
                                              :description @description}]))}
        [:section {:id "new-proxy-content"}
         [:> Grid
          [:> Grid.Col {:span {:base 12 :sm 6}}
           [:> Stack {:gap :lg}
            [forms/input-field {:placeholder "Proxy name"
                                :name "proxy-name"
                                :label "Name"
                                :on-change #(reset! name (.. % -target -value))
                                :description "Identify your proxy"
                                :required true}]
            [forms/input-field {:placeholder "https://internal-api-example.com"
                                :name "target-url"
                                :on-change #(reset! target-url (.. % -target -value))
                                :label "Target URL"
                                :description "The URL this proxy will point to"
                                :required true}]
            [forms/input-field {:placeholder "https://example.com"
                                :name "proxy-host"
                                :on-change #(reset! proxy-host (.. % -target -value))
                                :label "Proxy Host"
                                :description "The URL this proxy will be accessed from"
                                :required true}]
            [forms/textarea-field {:placeholder "Description"
                                   :name "description"
                                   :on-change #(reset! description (.. % -target -value))
                                   :label "Description"
                                   :description "(optional) Describe the purpose or a description for this proxy"}]]]]]
        [:> Box {:pos "absolute"
                 :w "100%"
                 :px "md"
                 :py "lg"
                 :style {:borderTop "1px solid var(--mantine-color-gray-2)"}
                 :bottom 0
                 :right 0}
         [:> Group {:gap :md
                    :align :center
                    :justify "end"}
          [:> Text {:color :gray
                    :size :sm}
           "You can edit it later if you need"]
          [button/secondary {:type "button"
                             :text "Cancel"}]
          [button/primary {:type "submit"
                           :text "Create proxy"}]]]]])))
