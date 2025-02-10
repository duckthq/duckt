(ns webclient.components.button
  (:require
    [reagent.core :as r]
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

(defn subtle [{:keys [text loading on-click full? type leftSection]}]
  [:> Button (merge
               {:variant "subtle"
                :onClick on-click
                :type (or type "button")
                :size "md"
                :color "dark"
                :loading loading}
               (when leftSection
                 {:leftSection (r/as-element [:> leftSection
                                              {:size 20
                                               :stroke 1.5}])})
               (when full? {:fullWidth true}))
   text])
