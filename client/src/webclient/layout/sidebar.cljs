(ns webclient.layout.sidebar
  (:require
    [reagent.core :as r]
    [re-frame.core :as rf]
    ["@tabler/icons-react" :refer [IconChevronDown IconCirclePlus IconCheck IconChartHistogram
                                   IconHome IconAffiliate IconUsers
                                   IconSettings IconListTree]]
    ["@mantine/core" :refer [Stack Group Box Text Menu Image
                             NavLink Divider Tooltip Anchor
                             useMantineTheme]]
    [webclient.routes :as routes]))

(defn- workspaces-menu-dropdown []
  (fn [workspaces selected-workspace]
    [:> Menu.Dropdown
     [:> Menu.Label "Your workspaces"]
     (for [workspace workspaces]
       ^{:key (:id workspace)}
       [:> Menu.Item
        (merge
          {:color :dark}
          (when (= (:id workspace) (:id selected-workspace))
            {:leftSection (r/as-element
                            [:> IconCheck {:size 16
                                           :color "var(--mantine-color-blue-5)"}])
             :color "blue"}))
          (:name workspace)])
       [:> Menu.Divider]
       [:> Menu.Item {:leftSection (r/as-element
                                     [:> IconCirclePlus
                                      {:size "16"
                                       :color "var(--mantine-color-blue-5)"
                                       :stroke "2"}])
                      :color "blue"
                      :onClick #(println :clicked)}
        "Create new workspace"]]))


(defn- sources []
  (let [proxies (rf/subscribe [:proxies])]
    (rf/dispatch [:proxies->get])

    (fn []
      [:> Stack
       [:> Group {:component  "header"
                  :style {:borderBottom "1px solid var(--mantine-color-gray-3)"}
                  :pb "4px"}
        [:> Text {:size :xs
                  :style {:flexGrow 1}
                  :fw 600
                  :color :gray}
         "Proxies"]
        [:> Tooltip {:label "Create new proxy"
                     :position :right
                     :withArrow true}
         [:> Anchor {:href (routes/url-for :new-proxy)}
          [:> IconCirclePlus
           {:color :gray
            :size 16}]]]]
       (when (and (not (:loading? @proxies))
                  (empty? (:data @proxies)))
         [:> NavLink {:href (routes/url-for :new-proxy)
                    :label "Add proxy"
                    :description "Set your first proxy"
                    :variant :light
                    :styles {:root {:border-radius "var(--mantine-radius-md)"}}
                    :color :blue
                    :active true
                    :leftSection (r/as-element [:> IconCirclePlus
                                                {:size 16
                                                 :color "var(--mantine-color-blue-6)"
                                                 :stroke "3"}])}])
       [:> Stack {:gap "2px"}
        (for [p (:data @proxies)]
          ^{:key (:id p)}
          [:> NavLink {:href (routes/url-for :proxy
                                             {:id (:id p)})
                       :label (:name p)
                       :variant :light
                       :styles {:root {:border-radius "var(--mantine-radius-md)"}}
                       :description (:target_url p)
                       :color :gray
                       :leftSection (r/as-element [:> IconAffiliate
                                                    {:size 16
                                                     :stroke "1.5"}])}
           [:> NavLink {:href (routes/url-for :proxy-overview
                                              {:proxy-id (:id p)})
                        :label "Overview"
                        :variant :light
                        :styles {:root {:border-radius "var(--mantine-radius-md)"}}
                        :color :gray
                        :leftSection (r/as-element [:> IconChartHistogram
                                                    {:size 16
                                                     :stroke "1.5"}])}]
           [:> NavLink {:href (routes/url-for :proxy-requests {:proxy-id (:id p)})
                        :label "Requests"
                        :variant :light
                        :styles {:root {:border-radius "var(--mantine-radius-md)"}}
                        :color :gray
                        :leftSection (r/as-element [:> IconListTree
                                                    {:size 16
                                                     :stroke "1.5"}])}]
           (comment [:> NavLink {:href (routes/url-for :proxy
                                              {:id (:id p)})
                        :label "Users"
                        :variant :light
                        :styles {:root {:border-radius "var(--mantine-radius-md)"}}
                        :color :gray
                        :leftSection (r/as-element [:> IconUsers
                                                    {:size 16
                                                     :stroke "1.5"}])}])
           [:> Divider]
           [:> NavLink {:href (routes/url-for :proxy-settings {:proxy-id (:id p)})
                        :label "Settings"
                        :variant :light
                        :styles {:root {:border-radius "var(--mantine-radius-md)"}}
                        :color :gray
                        :leftSection (r/as-element [:> IconSettings
                                                    {:size 16
                                                     :stroke "1.5"}])}]])]])))

(defn main []
  (let [user (rf/subscribe [:user->userinfo])
        workspaces (rf/subscribe [:workspaces])
        ;theme (js->clj (useMantineTheme) :keywordize-keys true)
        ]
    (rf/dispatch [:workspaces->get])
    (fn []
      (let [selected-workspace (first
                                 (filter #(= (:id %)
                                             (-> @user
                                                 :user_preferences
                                                 :selected_workspace))
                                         @workspaces))]
        [:div {:id "sidebar"
               :class "sidebar"}
         [:> Box {:class [:sidebar-content]
                  :bg "gray.1"}
          [:> Stack
           [:> Menu {:shadow :md
                     :withArrow true}
            [:> Menu.Target {:style {:cursor :pointer}}
             [:> Group {:gap :xs
                        :px "md"
                        :pt "md"
                        :mb "md"
                        :align :center}
              [:> Box {:w "36px"}
               [:> Image
                {:src "/images/brand/icon-black-white.svg"
                 :fit :contain
                 :w "100%"}]]
              [:> Box {:style {:flexGrow 1}}
               [:> Group {:gap :xs}
                [:> Box {:maw "120px"
                         :style {:flexGrow 1}}
                 [:> Text {:size :md
                           :fw 500
                           :color :dark
                           :truncate :end}
                  (:name selected-workspace)]]
                [:> IconChevronDown {:size "14"
                                     :color "var(--mantine-color-gray-8)"
                                     :stroke "3"}]]]]]
            [workspaces-menu-dropdown @workspaces selected-workspace]]
           [:> Stack {:p :xs}
            [:> NavLink {:href (routes/url-for :home)
                         :label "Home"
                         :styles {:root {:border-radius "var(--mantine-radius-md)"}}
                         :variant :light
                         ;:active true
                         :leftSection (r/as-element [:> IconHome
                                                     {:size 16
                                                      :stroke "1.5"}])}]
            [sources]]]]]))))
