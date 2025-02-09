(ns webclient.components.h
  (:require
    ["@mantine/core" :refer [Title Text Stack]]))

(defn page-title
  [text description]
  [:> Stack {:gap "0"}
   [:> Title {:order 1
              :fw 400
              :color :dark
              :size "h1"}
    text]
   (when description
     [:> Text {:color :gray
               :size :md}
      description])])

(defn h1
  [{:keys [text align]}]
  [:> Title {:order 1
             :align (or align "left")}
   text])

(defn h3
  [{:keys [text align]}]
  [:> Title {:order 3
             :align align}
   text])

(defn h5
  [{:keys [text align]}]
  [:> Title {:order 5
             :align align}
   text])
