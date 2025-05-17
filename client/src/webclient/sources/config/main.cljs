(ns webclient.sources.config.main
  (:require
    [re-frame.core :as rf]
    [webclient.sources.config.source-actions-list :as source-actions-list]
    ["@mantine/core" :refer [Paper Stack Text Title Button Group Accordion]]))

(defn main [source-id]
  (let [source (rf/subscribe [:sources->source-info])]
    (rf/dispatch [:sources->get-by-id source-id])
    (fn []
      (println :source @source)
      [:<>
       [:> Stack {:p :md}
        [:> Stack
         [:> Title {:order 1}
          "Source configuration"]]
        [:> Group {:gap :lg
                   :align :stretch
                   :grow 1}
         [:> Paper {:p :lg}
          [:> Stack
           [:> Stack {:gap :0}
            [:> Title {:order 4}
             "Source"]
            [:> Text {:color :dimmed}
             "Define step-by-step actions for your source"]]
           [source-actions-list/main (:data @source)]]]
         [:> Paper {:p :lg}
          [:> Stack
           [:> Stack {:gap 0}
            [:> Title {:order 4}
             "Saved Actions"]
            [:> Text {:color :dimmed}
             "Quick add previously saved actions"]]]]]]])))
