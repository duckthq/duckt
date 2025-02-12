(ns webclient.views
  (:require
    [re-frame.core :as rf]
    [bidi.bidi :as bidi]
    ["@mantine/core" :refer [MantineProvider createTheme
                             Container NavLink Anchor]]
    ["@mantine/notifications" :refer [Notifications]]
    [webclient.styles :as styles]
    [webclient.events.core]
    [webclient.routes :as routes]
    [webclient.subs :as subs]
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
    [webclient.endpoints.requests.panel :as endpoint-requests-panel]))

(defmethod routes/panels :home-panel []
  (set! (.-title js/document) "Home")
  [application/layout [home-panel/panel]])
(defmethod routes/panels :new-proxy-panel []
  (set! (.-title js/document) "New Proxy")
  [application/layout [new-proxy/panel]])
(defmethod routes/panels :new-proxy-success-panel []
  (set! (.-title js/document) "New Proxy")
  [application/layout [new-proxy-success/panel]])

(defmethod routes/panels :request-details-panel []
  (set! (.-title js/document) "Request Details")
  (let [pathname (.. js/window -location -pathname)
        current-route (bidi/match-route @routes/routes pathname)
        proxy-id (-> current-route :route-params :proxy-id)
        request-id (-> current-route :route-params :request-id)]

  [application/layout [request-details/main proxy-id request-id]]))

(defmethod routes/panels :proxy-overview-panel []
  (set! (.-title js/document) "Overview")
  (let [pathname (.. js/window -location -pathname)
        current-route (bidi/match-route @routes/routes pathname)
        proxy-id (-> current-route :route-params :proxy-id)]

  [application/layout [proxy-overview-panel/main proxy-id]]))

(defmethod routes/panels :proxy-requests-panel []
  (set! (.-title js/document) "Requests")
  (let [pathname (.. js/window -location -pathname)
        current-route (bidi/match-route @routes/routes pathname)
        proxy-id (-> current-route :route-params :proxy-id)]

  [application/layout [proxy-requests-panel/main proxy-id]]))

(defmethod routes/panels :proxy-customers-panel []
  (set! (.-title js/document) "Customers")
  (let [pathname (.. js/window -location -pathname)
        current-route (bidi/match-route @routes/routes pathname)
        proxy-id (-> current-route :route-params :proxy-id)]

  [application/layout [proxy-requests-panel/main proxy-id]]))

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

(defmethod routes/panels :login-panel [] [login/panel])
(defmethod routes/panels :signup-panel [] [signup/panel])

(def mantine-theme
  #js {:defaultRadius "md"
       :primaryColor "gray"
       :headings (clj->js {:sizes {:h3 {:fontWeight "400"}}})
       :white "#FAFAFA"
       :black "#363738"
       :cursorType :pointer
       :components {"NavLink"
                    (.extend
                      NavLink
                      {:vars (fn [_ props]
                               (js/console.log "props" props)
                               (if (= "gray" (.-color props))
                                 {"root" {"--nl-hover" "var(--mantine-color-gray-8)"}}
                                 {"root" {"--nl-hover" "red"}}))})}})

(defn main-panel []
  (let [active-panel (rf/subscribe [::subs/active-panel])
        ;serverinfo (rf/subscribe [:serverinfo])
        theme (rf/subscribe [:theme])]
    (styles/build-styles)
    (rf/dispatch [:serverinfo->get])
    (fn []
      [:> MantineProvider {:defaultColorScheme @theme
                           :forceColorScheme :light
                           :theme (createTheme mantine-theme)}
       [:> Notifications]
       [:> Container {:fluid true
                      :p "0"
                      :h "100%"}
        [routes/panels @active-panel]]])))
