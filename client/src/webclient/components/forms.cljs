(ns webclient.components.forms
  (:require
    ["@mantine/core" :refer [TextInput Textarea]]))

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
                   ;:styles {:input {:backgroundColor "#e1e1e1"}}
                   :type type})])

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
