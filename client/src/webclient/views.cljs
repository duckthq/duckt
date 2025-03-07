(ns webclient.views
  (:require
    [re-frame.core :as rf]
    [bidi.bidi :as bidi]
    ["@mantine/core" :refer [MantineProvider createTheme
                             Container MantineColorsTuple]]
    ["@mantine/notifications" :refer [Notifications]]
    [webclient.styles :as styles]
    [webclient.events.core]
    [webclient.routes :as routes]
    [webclient.subs]
    [webclient.layout.application :as application]
    ;; pages
    [webclient.auth.login :as login]
    [webclient.auth.signup :as signup]
    [webclient.proxies.new :as new-proxy]
    [webclient.proxies.new-proxy-success :as new-proxy-success]
    [webclient.proxies.overview.panel :as proxy-overview-panel]
    [webclient.proxies.requests.panel :as proxy-requests-panel]
    [webclient.proxies.settings.panel :as proxy-settings-panel]
    [webclient.proxies.request-details.panel :as request-details]
    [webclient.home.panel :as home-panel]
    [webclient.customers.panel :as customers-panel]
    [webclient.endpoints.requests.panel :as endpoint-requests-panel]
    [webclient.settings.users.panel :as users-settings-panel]
    ;; components
    [webclient.components.modal :as modal]))

(defmethod routes/panels :home-panel []
  (set! (.-title js/document) "Home")
  [application/layout [home-panel/panel]])
(defmethod routes/panels :new-proxy-panel []
  (set! (.-title js/document) "New Proxy")
  [application/layout [new-proxy/panel]])
(defmethod routes/panels :new-proxy-success-panel []
  (set! (.-title js/document) "New Proxy")
  [application/layout [new-proxy-success/panel]])

(defmethod routes/panels :customers-panel []
  (set! (.-title js/document) "Customers")
  [application/layout [customers-panel/main]])


(defmethod routes/panels :request-details-panel []
  (set! (.-title js/document) "Request Details")
  (let [pathname (.. js/window -location -pathname)
        current-route (bidi/match-route @routes/routes pathname)
        proxy-id (-> current-route :route-params :proxy-id)
        request-id (-> current-route :route-params :request-id)]

  [application/layout [request-details/main proxy-id request-id]]))

(defmethod routes/panels :proxy-overview-panel []
  (set! (.-title js/document) "Overview")
  [application/layout [proxy-overview-panel/main]])

(defmethod routes/panels :proxy-requests-panel []
  (set! (.-title js/document) "Requests")
  [application/layout [proxy-requests-panel/main]])

(defmethod routes/panels :proxy-settings-panel []
  (set! (.-title js/document) "Proxy settings")
  (let [pathname (.. js/window -location -pathname)
        current-route (bidi/match-route @routes/routes pathname)
        proxy-id (-> current-route :route-params :proxy-id)]

  [application/layout [proxy-settings-panel/main proxy-id]]))

(defmethod routes/panels :endpoint-requests-panel []
  (let [pathname (.. js/window -location -pathname)
        current-route (bidi/match-route @routes/routes pathname)
        endpoint-id (-> current-route :route-params :endpoint-id)]

  [application/layout [endpoint-requests-panel/main endpoint-id]]))

(defmethod routes/panels :user-settings-panel []
  (set! (.-title js/document) "Users settings")
  [application/layout [users-settings-panel/main]])

(defmethod routes/panels :login-panel [] [login/panel])
(defmethod routes/panels :signup-panel [] [signup/panel])
(defmethod routes/panels :default []
  [application/layout [:div "404 Not Found"]])

(def ^MantineColorsTuple gray-tuple [
  "#f1f4fe",
  "#e4e6ed",
  "#c8cad3",
  "#a9adb9",
  "#9094a3",
  "#7f8496",
  "#777c91",
  "#656a7e",
  "#595e72",
  "#4a5167"
])

(def mantine-theme
  (clj->js
    {:defaultRadius :md
     :primaryColor "gray"
     :colors {:grayTest gray-tuple}
     :headings (clj->js {:sizes {:h3 {:fontWeight "400"}}})
     ;:white "#FAFAFA"
     :black "#363738"
     :cursorType :pointer}))

(defn main-panel []
  (let [active-panel (rf/subscribe [:active-panel])
        ;serverinfo (rf/subscribe [:serverinfo])
        theme (rf/subscribe [:theme])]
    (styles/build-styles)
    (rf/dispatch [:serverinfo->get])
    (fn []
      (println :debug :active-panel @active-panel)
      [:> MantineProvider {:defaultColorScheme @theme
                           :forceColorScheme :light
                           :theme (createTheme mantine-theme)}
       [:> Notifications]
       [modal/main]
       [:> Container {:fluid true
                      :p "0"
                      :h "100%"}
        (routes/panels @active-panel)]])))
