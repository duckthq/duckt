(ns webclient.auth.signup
  (:require
    ["@mantine/core" :refer [Box Text Stack Container Group Image]]
    [clojure.string :as string]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [webclient.routes :as routes]
    [webclient.components.forms :as forms]
    [webclient.components.ui.title :as title]
    [webclient.components.button :as button]))

(defn panel []
  (let [name-field (r/atom "")
        email-field (r/atom "")
        password-field (r/atom "")
        repeat-password-field (r/atom "")
        invite-code (r/atom nil)
        serverinfo (rf/subscribe [:serverinfo])
        on-submit #(do (.preventDefault %)
                       (when (and (= @password-field @repeat-password-field)
                                  (not (string/blank? @password-field)))
                         (rf/dispatch [:auth->signup
                                       (merge
                                         (when @invite-code
                                           {:invite_code @invite-code})
                                         {:fullname @name-field
                                          :email @email-field
                                          :password @password-field})])))]
    (fn []
      [:> Container {:size "xs"}
       [:> Group {:h "100vh"
                  :justify "center"}
        [:> Stack {:gap "lg"
                   :align "stretch"}
         [:> Box {:w "42px"
                  :component :figure}
          [:> Image
           {:src "/images/brand/icon-green-white.svg"
            :fit true}]]
         [:> Stack {:gap "xs"}
          [title/h3 "Create your account"]
          [:> Text {:color "gray"}
           "Welcome to a new way of seeing your data."]]
         [:form {:on-submit on-submit}
          [:> Stack {:gap "lg"}
           [:> Stack {:gap "sm"}
            [forms/input-field {:placeholder "Name"
                                :on-change #(reset! name-field (.. % -target -value))
                                :required true
                                :name "email"}]
            [forms/input-field {:placeholder "Email"
                                :on-change #(reset! email-field (.. % -target -value))
                                :required true
                                :name "email"}]
            [forms/input-field {:placeholder "Password"
                                :on-change #(reset! password-field (.. % -target -value))
                                :name "password"
                                :required true
                                :autoComplete :on
                                :type "password"}]
            [forms/input-field (merge
                                 {:placeholder "Repeat your password"
                                  :on-change #(reset! repeat-password-field (.. % -target -value))
                                  :name "repeat-password"
                                  :required true
                                  :autoComplete :on
                                  :type "password"}
                                 (when (and (not= @password-field @repeat-password-field)
                                            (not (string/blank? @password-field))
                                            (not (string/blank? @repeat-password-field)))
                                   {:error "Passwords do not match"}))]]
           (when (:invite-only? @serverinfo)
             [forms/input-field {:placeholder "XXXX-XXXX-XXXX"
                                 :required true
                                 :onChange #(reset! invite-code (.. % -target -value))
                                 :name "invitation-code"
                                 :label "Invitation code"
                                 :description "We are invite-only for now."
                                 :type "text"}])
           [button/Primary
            {:onClick on-submit}
            "Create your account"]]]
         [:footer
          [:div
           [:> Text {:color "dark"}
            "Already have an account? "
            [:a {:class "anchor"
                 :href (routes/url-for :login)}
             "Sign in here."]]]]]]])))
