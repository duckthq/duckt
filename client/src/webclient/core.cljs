(ns webclient.core
  (:require
   [reagent.core :as r]
   [goog.dom :as gdom]
   ["react-dom/client" :refer [createRoot]]
   [re-frame.core :as re-frame]
   ["dayjs/plugin/utc" :as dayjs-utc]
   ["dayjs" :as dayjs]
   [webclient.events.core :as events]
   [webclient.routes :as routes]
   [webclient.views :as views]
   [webclient.config :as config]))

;; extend dayjs to use UTC
(.extend dayjs dayjs-utc)

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defonce root (createRoot (gdom/getElement "app")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (.render root (r/as-element [views/main-panel])))

(defn init []
  (routes/start!)
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
