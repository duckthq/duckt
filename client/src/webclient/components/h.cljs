(ns webclient.components.h
  (:require
    ["@mantine/core" :refer [Title Text Stack]]
    [webclient.components.ui.text :as text]))

(defn page-title
  [text description]
  [:> Stack {:gap "0"}
   [:> Title {:order 1
              :color :dark
              :size "h1"}
    text]
   (when description
     [:> Text {:size :md
               :color :dimmed}
      description])])
