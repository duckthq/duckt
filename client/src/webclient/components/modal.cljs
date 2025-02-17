(ns webclient.components.modal
  (:require
    [re-frame.core :as rf]
    [reagent.core :as r]
    ["@mantine/core" :refer [Modal]]))

(defn main []
  (let [modals (rf/subscribe [:modals])]
    (fn []
      (let [active-modal (last @modals)]
        (println :modals @modals)
        [:<>
         [:> Modal
          (merge
            {:title (:title active-modal)
             :centered true
             :opened (not (nil? active-modal))
             :onClose (fn [] (rf/dispatch [:modal->close]))
             :size :md}
            (:props active-modal))
          (:content active-modal)]]))))
