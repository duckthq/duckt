(ns webclient.components.ui.title
  (:require
    ["@mantine/core" :refer [Title Text Stack]]
    [webclient.components.ui.text :as text]))

(defn page-title
  [text description]
  [:> Stack {:gap "0"}
   [:> Title {:order 1
              :fw 400
              :color :dark
              :size "h1"}
    text]
   (when description
     [text/Dimmed {:size :md}
      description])])

(defn h1
  ([{:keys [align]} text]
  [:> Title {:order 1
             :align align}
   text])
  ([text] [h1 {} text]))

(defn h3
  ([{:keys [align]} text]
  [:> Title {:order 3
             :align align}
   text])
  ([text] [h3 {} text]))

(defn h4
  ([{:keys [align]} text]
  [:> Title {:order 4
             :align align}
   text])
  ([text] [h4 {} text]))

(defn h5
  ([{:keys [align]} text]
  [:> Title {:order 5
             :align align}
   text])
  ([text] [h5 {} text]))
