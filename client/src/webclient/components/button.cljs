(ns webclient.components.button
  (:require
    [reagent.core :as r]
    ["@mantine/core" :refer [Button]]))

(defn- base [props text]
  [:> Button (merge
               props
               {:type (or (:type props) "button")
                :size (or (:size props) :md)})
   text])

(defn Primary
  ([props el]
   [base (merge props {:variant :filled
                       :color :dark})
    el])
  ([el] [Primary {} el]))

(defn Secondary
  ([props el]
   [base (merge props {:variant :light
                       :color :dark})
    el])
  ([el] [Secondary {} el]))

;; TODO: deprecate this, Primary is a better implementation
(defn primary [{:keys [text loading on-click full?
                       type size]}]
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
