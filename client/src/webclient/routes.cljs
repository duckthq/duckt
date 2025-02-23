(ns webclient.routes
  (:require
   [bidi.bidi :as bidi]
   [pushy.core :as pushy]
   [clojure.string :as string]
   [re-frame.core :as re-frame]
   [webclient.events.core :as events]))

(defmulti panels identity)

(def routes
  (atom
    [""
     {"/" :home
      "/customers" :customers
      "/endpoints" [[["/" :endpoint-id "/requests"] :endpoint-requests]]
      "/login" :login
      "/proxies" [[["/new"] :new-proxy]
                 ["/new/success" :new-proxy-success]
                 [["/" :proxy-id "/overview"] :proxy-overview]
                 [["/" :proxy-id "/requests"] :proxy-requests]
                 [["/" :proxy-id "/customers"] :proxy-customers]
                 [["/" :proxy-id "/settings"] :proxy-settings]
                 [["/" :proxy-id "/requests/" :request-id] :request-details]
                 ["" :proxies]]
      "/signup" :signup
      "/settings/users" :user-settings}]))

(defn query-params-parser
  [queries]
  (let [url-search-params (new js/URLSearchParams (clj->js queries))]
    (if (string/blank? (.toString url-search-params))
      (str "?" (.toString url-search-params))
      "")))

(defn parse
  [url]
  (bidi/match-route @routes url))

(defn url-for
  [& args]
  (apply bidi/path-for (into [@routes] (flatten args))))

(defn dispatch
  [route]
  (let [panel (keyword (str (name (:handler route)) "-panel"))]
    (re-frame/dispatch [::events/set-active-panel panel])))

(defonce history
  (pushy/pushy dispatch parse))

(defn navigate!
  [config]
  (let [uri (str (url-for (:handler config)
                          (or (:params config) []))
                 (:query-params config))]
    (pushy/set-token! history uri)))

(defn start!
  []
  (pushy/start! history))

(defn get-page-param
  "Receives a keyword representing the param name and returns the value of the param"
  [param-name]
  (let [pathname (.. js/window -location -pathname)
        current-route (bidi/match-route @routes pathname)]
  (-> current-route :route-params param-name)))

(re-frame/reg-fx
 :navigate
 (fn [config]
   (navigate! {:handler (:handler config)
               :params (:params config)
               :query-params (query-params-parser
                               (or (:query-params config) {}))})))
