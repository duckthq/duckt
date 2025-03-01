(ns webclient.components.clipboard
  (:require
    [reagent.core :as r]
    ["@mantine/core" :refer [Button Tooltip]]
    ["@tabler/icons-react" :refer [IconCopy]]))

(defn icon []
  (let [copied (r/atom false)
        copy-to-clipboard (fn [text]
                            (reset! copied true)
                            (js/navigator.clipboard.writeText text)
                            (js/setTimeout #(reset! copied false) 1000))]
    (fn [{:keys [text]}]
      [:div
      [:> Tooltip {:label (if @copied "Copied!" "Copy to clipboard")
                   :withArrow true
                   :closeDelay (if @copied 500 100)}
       [:> Button {:variant :subtle
                   :p "2px"
                   :radius :sm
                   :size :compact-xs
                   :onClick #(copy-to-clipboard text)}
        [:> IconCopy {:size 16
                      :color (if copied
                               "var(--mantine-color-gray-8)"
                               "var(--mantine-color-gray-6)")}]]]])))
