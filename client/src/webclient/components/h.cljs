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
  ([{:keys [align]} text]
  [:> Title {:order 3
             :align align}
   text])
  ([text] (h3 {} text)))

(defn h4
  ([{:keys [align]} text]
  [:> Title {:order 4
             :align align}
   text])
  ([text] (h4 {} text)))

(defn h5
  ([{:keys [align]} text]
  [:> Title {:order 5
             :align align}
   text])
  ([text] (h5 {} text)))
