(ns webclient.layout.application
  (:require
    ["@mantine/core" :refer [Box]]
    [webclient.layout.topbar :as topbar]
    [webclient.layout.sidebar :as sidebar]))

(defn layout [panel]
  [:div {:height "100vh"}
   ;; sidebar
   [sidebar/main]
   ;; main container
   [:> Box {:ml "250px"
            :h "100vh"}
    [topbar/main]
    [:> Box {:p 0
             :pos "relative"
             :mih "calc(100vh - 71px)"} ;; 71px is the height of the topbar
     panel]]])
