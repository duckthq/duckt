(ns webclient.views
  (:require
    [re-frame.core :as rf]
    [bidi.bidi :as bidi]
    ["@mantine/core" :refer [MantineProvider createTheme
                             Container virtualColor
                             NavLink]]
    ["@mantine/colors-generator" :refer [generateColors]]
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
  (let [pathname (.. js/window -location -pathname)
        current-route (bidi/match-route @routes/routes pathname)
        request-id (-> current-route :route-params :request-id)]

  [application/layout [request-details/main request-id]]))

(defmethod routes/panels :proxy-overview-panel []
  (set! (.-title js/document) "Overview")
  (let [pathname (.. js/window -location -pathname)
        current-route (bidi/match-route @routes/routes pathname)
        proxy-id (-> current-route :route-params :proxy-id)]

  [application/layout [proxy-overview-panel/main proxy-id]]))

(defmethod routes/panels :proxy-requests-panel []
  (let [pathname (.. js/window -location -pathname)
        current-route (bidi/match-route @routes/routes pathname)
        proxy-id (-> current-route :route-params :proxy-id)]

  [application/layout [proxy-requests-panel/main proxy-id]]))

(defmethod routes/panels :proxy-customers-panel []
  (let [pathname (.. js/window -location -pathname)
        current-route (bidi/match-route @routes/routes pathname)
        proxy-id (-> current-route :route-params :proxy-id)]

  [application/layout [proxy-requests-panel/main proxy-id]]))

(defmethod routes/panels :endpoint-requests-panel []
  (let [pathname (.. js/window -location -pathname)
        current-route (bidi/match-route @routes/routes pathname)
        endpoint-id (-> current-route :route-params :endpoint-id)]

  [application/layout [endpoint-requests-panel/main endpoint-id]]))

(defmethod routes/panels :login-panel [] [login/panel])
(defmethod routes/panels :signup-panel [] [signup/panel])

(def mantine-theme
  #js {:fontFamily "Open Sans, sans-serif"
       :defaultRadius "md"
       :primaryColor "gray"
       :headings #js {:sizes #js{:h3 #js {:fontWeight "400"}}}
       :colors #js {"primary-blue" (generateColors "#1746A2")
                    :dark-blue (virtualColor
                                 #js {:name "dark-blue"
                                      :dark "#001340"
                                      :light "#FAFAFA"})
                    :primary (virtualColor
                               #js {:name "primary"
                                    :dark "#1746A2"
                                    :light "#FAFAFA"})}
       :white "#FAFAFA"
       ;; TODO: see why this doesn't work later
       :components {"NavLink"
                    (.extend
                      NavLink
                      {:vars (fn [_ props]
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
                           :theme (createTheme mantine-theme)}
       [:> Container {:fluid true
                      :p "0"
                      :h "100%"}
        [routes/panels @active-panel]]])))
