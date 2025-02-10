(ns webclient.layout.topbar
  (:require
    [re-frame.core :as rf]
    [reagent.core :as r]
    [clojure.string :as string]
    ["@tabler/icons-react" :refer [IconSearch IconChevronDown
                                   IconHome IconDashboard IconFileText]]
    ["@mantine/spotlight" :refer [Spotlight spotlight]]
    ["@mantine/core" :refer [Group Avatar TextInput
                             Code Box Menu Group Text]]))

(def spotlight-actions
  [{
    :id "home",
    :label "Home",
    :description "Get to home page",
    :onClick #(println "Home")
    :leftSection (r/as-element [:> IconHome {:size 24 :stroke 1.5}]),
    }
   {
    :id "dashboard",
    :label "Dashboard",
    :description "Get full information about current system status"
    :onClick #(println "Dashboard")
    :leftSection (r/as-element
                   [:> IconDashboard {:size 24 :stroke 1.5}]),
    },
   {
    :id "documentation",
    :label "Documentation",
    :description "Visit documentation to lean more about all features"
    :onClick (println "Documentation")
    :leftSection (r/as-element
                   [:> IconFileText {:size 24 :stroke 1.5}])
    }])

(defn main []
  (let [is-mac? (rf/subscribe [:is-mac?])
        user (rf/subscribe [:user->userinfo])]
    (rf/dispatch [:serverinfo->get])
    (rf/dispatch [:user->get-userinfo])
    ;; dispatches search modal
    (.addEventListener
      js/document "keydown"
      (fn [e]
        (when (and (or (= (.-key e) "k")
                       (= (.-key e) "K"))
                   (or (.-metaKey e)
                       (.-ctrlKey e)))
          (.open spotlight))))
    (fn []
      [:> Box {:id "topbar"
               :style {:z-index 2}
               :top 0
               :left 0
               :class "topbar"}
       [:> Spotlight {:actions spotlight-actions}]
       [:> Box {:px "sm"
                :py "md"}
        [:> Group {:justify "space-between"
                   :align "center"}
         [:> TextInput
          {:placeholder "Search..."
           :on-click #(.open spotlight)
           :variant "filled"
           :miw "300px"
           :rightSectionPointerEvents "none"
           :leftSectionPointerEvents "none"
           :leftSection (r/as-element
                          [:> IconSearch {:size "16"
                                          :color "var(--mantine-color-gray-4)"
                                          :stroke "3"}])
           :rightSectionWidth "md"
           :rightSection (r/as-element [:> Code
                                        (if is-mac?
                                          "⌘ + k"
                                          "ctrl + k")])}]
         [:> Menu {:width "200px"
                   :shadow :md
                   :withArrow true}
          [:> Menu.Target
           [:> Group {:align :center
                      :gap "4px"}
            [:> Avatar {:color "initials"
                        :component "a"
                        :href "#"
                        :variant "light"
                        :size "md"
                        :name (or (:fullname @user)
                                  (:email @user))}]
            [:> Group {:style {:cursor :pointer}
                       :gap (if (:fullname @user) "4px" "0")}
             [:> Text {:size "sm"
                       :color "dark"}
              (if (:fullname @user)
                (first (string/split (:fullname @user) #" "))
                (:email @user))]
             [:> IconChevronDown {:size "14"
                                  :color "var(--mantine-color-gray-8)"
                                  :stroke "3"}]]]]
          [:> Menu.Dropdown
           [:> Menu.Label "Settings"]
           [:> Menu.Item "Profile"]
           [:> Menu.Divider]
           [:> Menu.Item
            {:color "red"
             :onClick #(rf/dispatch [:auth->logout])}
            "Logout"]]]]]])))
