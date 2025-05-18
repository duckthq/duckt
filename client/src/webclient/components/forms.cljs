(ns webclient.components.forms
  (:require
    ["@mantine/core" :refer [TextInput Textarea Select]]))

(defn input-field
  [{:keys [placeholder type on-change onChange required error
           label description input-name id defaultValue
           autoComplete]}]
  [:> TextInput (merge
                  (when on-change {:onChange on-change})
                  (when onChange {:onChange onChange})
                  (when required {:required true
                                  :withAsterisk true})
                  (when error {:error error})
                  (when autoComplete {:autoComplete autoComplete})
                  {:placeholder placeholder
                   :id id
                   :label label
                   :defaultValue defaultValue
                   :name input-name
                   :description description
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
                   :description description})])
