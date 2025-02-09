(ns webclient.auth.signup
  (:require
    ["@mantine/core" :refer [Text Stack Container Group]]
    [clojure.string :as string]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [webclient.routes :as routes]
    [webclient.components.forms :as forms]
    [webclient.components.h :as h]
    [webclient.components.button :as button]))

(defn panel []
  (let [name-field (r/atom "")
        email-field (r/atom "")
        password-field (r/atom "")
        repeat-password-field (r/atom "")
        on-submit #(do (.preventDefault %)
                       (when (and (= @password-field @repeat-password-field)
                                  (not (string/blank? @password-field)))
                         (rf/dispatch [:auth->signup
                                       {:fullname @name-field
                                        :email @email-field
                                        :password @password-field}])))]
    (fn []
      [:> Container {:size "xs"}
       [:> Group {:class "h-screen"
                  :justify "center"}
        [:div {:class "w-full"}
         [:> Stack {:gap "lg"
                    :align "stretch"}
          [:figure {:class "w-12 mb-2"}
           [:img {:src "/images/brand/icon-green-white.svg"}]]
          [:> Stack {:gap "xs"}
           [h/h3 {:text "Create your account"}]
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
                                 :type "password"}]
             [forms/input-field (merge
                                  {:placeholder "Repeat your password"
                                   :on-change #(reset! repeat-password-field (.. % -target -value))
                                   :name "repeat-password"
                                   :required true
                                   :type "password"}
                                  (when (and (not= @password-field @repeat-password-field)
                                             (not (string/blank? @password-field))
                                             (not (string/blank? @repeat-password-field)))
                                    {:error "Passwords do not match"}))]]
            ;[forms/input-field {:placeholder "XCXCXCXC-XXXX-XXXX-XXXX-XXXXXXXXXXXX"
                                 ;                    :name "invitation-code"
                                 ;                    :label "Invitation code"
                                 ;                    :description "We are invite-only for now."
                                 ;                    :required true
                                 ;                    :type "text"}]]
           [button/primary
            {:full? true
             :type "submit"
             :text "Create your account"}]]]
         [:footer
          [:div
           [:> Text {:color "dark"}
            "Already have an account? "
            [:a {:class "anchor"
                 :href (routes/url-for :login)}
             "Sign in here."]]]]]]]])))
