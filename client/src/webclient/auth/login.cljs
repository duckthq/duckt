(ns webclient.auth.login
  (:require
    [re-frame.core :as rf]
    [reagent.core :as r]
    ["@mantine/core" :refer [Box Text Stack Container Group Image Title]]
    [webclient.routes :as routes]
    [webclient.components.forms :as forms]
    [webclient.components.button :as button]))

(defn panel []
  (let [email (r/atom "")
        password (r/atom "")
        on-submit #(do (.preventDefault %)
                       (rf/dispatch [:auth->login
                                     {:email @email
                                      :password @password}]))]
    (fn []
      [:> Container {:size "xs"}
       [:> Group {:h "100vh"
                  :justify "center"}
        [:> Stack {:gap "lg"
                   :align "stretch"}
         [:> Box {:w "42px"}
          [:> Image
           {:src "/images/brand/icon-green-white.svg"
            :fit true}]]
         [:> Stack {:gap "xs"}
          [:> Title {:order 3} "Login to Duckt"]
          [:> Text {:color "gray"}
           "Welcome to a new way of seeing your data."]]
         [:form {:on-submit on-submit}
          [:> Stack {:gap "lg"}
           [:> Stack {:gap "sm"}
            [forms/input-field {:placeholder "Email"
                                :on-change #(reset! email (.. % -target -value))
                                :required true
                                :name "email"}]
            [forms/input-field {:placeholder "Password"
                                :on-change #(reset! password (.. % -target -value))
                                :name "password"
                                :autoComplete :on
                                :required true
                                :type "password"}]]
           [button/primary
            {:type "submit"
             :full? true
             :text "Login"}]]]
         [:footer
          [:div
           [:> Text {:color "dark"}
            "Don't have an account? "
            [:a {:class "anchor"
                 :href (routes/url-for :signup)}
             "Sign up here."]]]]]]])))
