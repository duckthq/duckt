(ns webclient.components.forms
  (:require
    ["@mantine/core" :refer [TextInput Textarea Select]]))

(defn input-field
  [{:keys [placeholder type on-change onChange required error
           label description input-name id defaultValue]}]
  [:> TextInput (merge
                  (when on-change {:onChange on-change})
                  (when onChange {:onChange onChange})
                  (when required {:required true
                                  :withAsterisk true})
                  (when error {:error error})
                  {:placeholder placeholder
                   :id id
                   :label label
                   :defaultValue defaultValue
                   :name input-name
                   :variant "filled"
                   :size "md"
                   :description description
                   :classNames {:input "input-field"}
                   :type type})])

(defn select-field
  [{:keys [placeholder required error data
           disabled
           label description input-name id size
           defaultValue onChange options]}]
  [:> Select (merge
               (when onChange {:onChange onChange})
               (when required {:required true
                               :withAsterisk true})
               (when error {:error error})
               (when disabled {:disabled disabled})
               {:placeholder placeholder
                :classNames {:input "input-field"}
                :id id
                :label label
                :data data
                :name input-name
                :defaultValue defaultValue
                :variant :filled
                :size (or size :md)
                :description description
                :options options})])

(defn textarea-field
  [{:keys [placeholder on-change required error
           label description input-name id
           defaultValue onChange]}]
  [:> Textarea (merge
                  (when on-change {:onChange on-change})
                  (when onChange {:onChange onChange})
                  (when required {:required true
                                  :withAsterisk true})
                  (when error {:error error})
                  {:placeholder placeholder
                   :id id
                   :label label
                   :name input-name
                   :defaultValue defaultValue
                   :variant "filled"
                   :size "md"
                   :multiline true
                   :description description
                   :classNames {:input "input-field"}
                   ;:styles {:input {:backgroundColor "#e1e1e1"}}
                   })])
