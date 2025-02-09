(ns webclient.proxies.request-details.panel
  (:require
    [re-frame.core :as rf]
    [reagent.core :as r]))

(defn main [request-id]
  [:div
   [:h1 "Request Details"]
   [:p "Request ID: " request-id]])
