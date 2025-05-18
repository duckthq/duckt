(ns webclient.settings.users.panel
  (:require
    [re-frame.core :as rf]
    [reagent.core :as r]
    ["@mantine/core" :refer [Text Stack Group Box Divider Avatar
                             Button Paper]]
    ["@tabler/icons-react" :refer [IconUserPlus]]
    [webclient.components.ui.text :as text]
    [webclient.components.h :as title]
    [webclient.components.button :as button]
    [webclient.components.forms :as forms]))

(defn new-user-modal-content []
  (declare _)
  (r/with-let [_ true]
    (let [new-user-info (rf/subscribe [:users->new-user-information])
          email (r/atom "")
          fullname (r/atom "")
          role (r/atom "member")
          create-user! #(do (.preventDefault %)
                         (rf/dispatch [:users->add
                                       {:email @email
                                        :fullname @fullname
                                        :role @role}]))]
      ;; clean up possible just generated user from state
      (rf/dispatch [:users->reset-new-user-information])
      (fn []
        (if (= (:status @new-user-info) :success)
          [:> Stack
           [:> Text
            (str "The user "
                 (-> @new-user-info :data :email)
                 " was added successfully")]
           [:> Stack {:gap 0}
            [:> Text {:fw 600
                      :component :span
                      :size :sm}
             "A default password was generated for the user."]
            [:> Text {:fw 600
                      :component :span
                      :size :sm}
             "They will be requested to reset it during first login."]]
           [:> Text {:size :sm}
            "Temporary password:"]
           [:> Paper {:bg "gray.1"
                      :p :sm}
            [:> Text {:size :sm}
             (-> @new-user-info :data :password)]]]
          ;else
          [:> Stack {:gap "lg"}
           [:> Stack {:gap "sm"}
            [forms/input-field {:placeholder "Full name"
                                :label "Full name"
                                :defaultValue @fullname
                                :onChange #(reset! fullname (.. % -target -value))
                                :required true
                                :name "fullname"}]
            [forms/input-field {:placeholder "Email"
                                :defaultValue @email
                                :onChange #(reset! email (.. % -target -value))
                                :label "Email"
                                :required true
                                :name "email"}]
            [forms/select-field {:defaultValue @role
                                 :label "Role"
                                 :onChange (fn [value] (reset! role value))
                                 :required true
                                 :data ["member" "admin" "owner"]}]]
           [:> Group {:grow 1}
            [button/Secondary
             {:onClick #(rf/dispatch [:modal->close])}
             "Cancel"]
            [button/Primary
             {:onClick create-user!}
             "Add user"]]])))
    (finally #(rf/dispatch [:users->reset-new-user-information]))))

(defn users-list []
  (fn [users userinfo]
    [:> Stack
     (doall
       (for [user users]
         ^{:key (:id user)}
         [:> Box
          [:> Group {:p :md}
           [:> Group {:style {:flex-grow 1}}
            [:> Avatar {:color "initials"
                        :variant :filled
                        :size :sm
                        :name (:fullname user)}]
            [:> Stack {:gap 0}
             [text/Base {:size :md}
              (:fullname user)]
             [text/Dimmed {:size :xs}
              (:email user)]]]
           (if (contains? #{"admin" "owner"} (:role userinfo))
             [:<>
              [forms/select-field {:defaultValue (:role user)
                                   :onChange #(rf/dispatch [:users->update-role (:id user) %])
                                   :disabled (= (count users) 1)
                                   :size :xs
                                   :data ["member" "admin" "owner"]}]
              [:> Divider {:orientation :vertical}]
              [:> Button {:size :xs
                          :onClick #(rf/dispatch [:users->delete (:id user)])
                          :variant :transparent
                          :disabled (= (count users) 1)
                          :color :red}
               "Delete"]]
             [:> Text {:size :sm} (:role user)])]
          [:> Divider]]))]))

(defn main []
  (let [users (rf/subscribe [:users])
        userinfo (rf/subscribe [:user->userinfo])]
    (rf/dispatch [:users->get])
    (fn []
      [:> Stack {:p :md
                 :gap :xl}
       [:> Group
        [:> Box {:style {:flex-grow 1}}
         [title/page-title "Users settings"]]
        (when (contains? #{"admin" "owner"} (:role @userinfo))
          [button/Primary
           {:onClick #(rf/dispatch [:modal->open {:content [new-user-modal-content]
                                                  :title "Add new user"}])
            :size :sm
            :leftSection (r/as-element [:> IconUserPlus {:size 16}])}
           "Add user"])]
       [users-list (:data @users) @userinfo]])))
