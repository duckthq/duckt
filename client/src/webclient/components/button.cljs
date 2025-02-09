(ns webclient.components.button
  (:require
    ["@mantine/core" :refer [Button]]))

(defn primary [{:keys [text loading on-click full?
                       type]}]
  [:> Button (merge
               {:variant "filled"
                :onClick on-click
                :type (or type "button")
                :size "md"
                :color "dark"
                :loading loading}
               (when full? {:fullWidth true}))
   text])

(defn secondary [{:keys [text loading on-click full?
                         type]}]
  [:> Button (merge
               {:variant "light"
                :onClick on-click
                :type (or type "button")
                :size "md"
                :color "dark"
                :loading loading}
               (when full? {:fullWidth true}))
   text])
