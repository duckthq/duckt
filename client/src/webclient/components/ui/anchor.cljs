(ns webclient.components.ui.anchor
  (:require ["@mantine/core" :refer [Anchor]]))

(defn Base
  ([{:keys [color] :as props} el]
   [:> Anchor
    (merge
      props
      {:size :sm
       :c (or color "var(--mantine-color-blue-7)")})
    el])
  ([el] [:> Anchor {} el]))

(defn Dark
  ([props el]
   [Base
    (merge
      props
      {:color :dark})
    el])
  ([el] [Dark {} el]))
