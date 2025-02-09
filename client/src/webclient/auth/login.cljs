(ns webclient.auth.login
  (:require
    [re-frame.core :as rf]
    [reagent.core :as r]
    ["@mantine/core" :refer [Text Stack Container Group]]
    [webclient.routes :as routes]
    [webclient.components.forms :as forms]
    [webclient.components.h :as h]
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
       [:> Group {:class "h-screen"
                  :justify "center"}
        [:div {:class "w-full"}
         [:> Stack {:gap "lg"
                    :align "stretch"}
          [:figure {:class "w-12 mb-2"}
           [:img {:src "/images/brand/icon-green-white.svg"}]]
          [:> Stack {:gap "xs"}
           [h/h3 {:text "Login to Mainframe"}]
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
              "Sign up here."]]]]]]]])))
