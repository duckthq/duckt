(ns webclient.sources.new-source-success
  (:require
    [reagent.core :as r]
    [re-frame.core :as rf]
    ["@mantine/core" :refer [Stack Group Paper Code Tabs Button
                             Title Table Timeline Text Anchor Divider]]
    ["@tabler/icons-react" :refer [IconBrandDocker IconExternalLink IconCpu
                                   IconBrandGithubFilled IconDownload IconTerminal]]
    ["js-confetti" :as JSConfetti]
    [webclient.components.clipboard :as clipboard]))

(defn docker-installation []
  (let [architectures [{:value "intel"
                        :title "Intel Based Chips"
                        :image "duckthq/proxy"}
                       {:value "arm"
                        :title "ARM64/Apple Silicon"
                        :image "duckthq/proxy-arm64"}]]
    (fn [proxy-info]
      [:> Stack {:gap :lg}
       [:> Tabs {:defaultValue "intel"}
        [:> Tabs.List
         [:> Tabs.Tab {:value "intel"} "Intel Based Chips"]
         [:> Tabs.Tab {:value "arm"} "ARM64/Apple Silicon"]]

        (doall
          (for [a architectures]
            ^{:key (:value a)}
            [:> Tabs.Panel {:value (:value a)
                            :py :lg
                            :px :md}
             [:> Stack {:gap :lg}
              [:> Group
               [:> Text {:color :dimmed} "Image:"]
               [:> Code (:image a)]
               [clipboard/icon {:text (:image a)}]]

              [:> Table {:variant :vertical}
               [:> Table.Tbody
                [:> Table.Tr
                 [:> Table.Th "Environment Variable"]
                 [:> Table.Th "Value"]]
                (comment
                  [:> Table.Tr
                   [:> Table.Th "DUCKT_SERVER_URL"]
                   [:> Table.Td "URL of the Duckt server"]])
                [:> Table.Tr
                 [:> Table.Th "PROXY_TOKEN"]
                 [:> Table.Td (:proxy-key proxy-info)]]]]
              [:> Stack
               [:> Title {:order 5} "You may also run the container locally"]
               [:> Paper {:withBorder true
                          :p :md}
                [:> Group
                 [:> Code
                  (str "docker run --rm -it -p 4445:4445"
                       " -e PROXY_TOKEN=" (:proxy-key proxy-info) " " (:image a))]]]]]]))]])))

(defn bare-metal-installation []
  (fn [proxy-info]
    [:> Stack
     [:> Text "You can download the standalone jar file and run it on any machine with Java installed."]
     [:> Timeline
      [:> Timeline.Item {:title "Download the standalone jar file"
                         :bullet (r/as-element [:> IconBrandGithubFilled {:size 16}])}
       [:> Text
        [:span
         [:span "Download the latest proxy standalone jar file from the "]
         [:> Anchor {:href "https://github.com/duckthq/duckt/releases"
                     :target "_blank"}
          [:span
           [:span "Duckt GitHub releases page."]
           [:> IconExternalLink {:size 14}]]]]]]
      [:> Timeline.Item {:title "Install Java"
                         :bullet (r/as-element [:> IconDownload {:size 16}])}
       [:> Text [:span [:span "Make sure you have Java installed on your machine. "]
                   [:> Anchor
                    {:href "https://www.java.com/en/download/help/download_options.html"
                     :target "_blank"}
                    "You can download Java from the official website."]]]]
      [:> Timeline.Item {:title "Run the jar file"
                         :bullet (r/as-element [:> IconTerminal {:size 16}])}
       [:> Text "Run the jar file replacing <version> with the actual downloaded version"]
       [:> Paper {:withBorder true
                  :p :md}
        [:> Group
         [:> Code (str
                    "PROXY_TOKEN=" (:proxy-key proxy-info) " "
                    "java -jar proxy-standalone-<version>.jar")]]]]]]))

(defn panel []
  (let [new-source-info (rf/subscribe [:sources->new-source-info])
        js-confetti (new JSConfetti)]
    (fn []
      (.addConfetti js-confetti
                    (clj->js {:confettiRadius 2
                              :confettiColors ["#999" "#ddd" "#aaa"]
                              :confettiNumber 3000}))
      [:> Stack {:p :md
                 :gap :xl}
       [:> Stack {:gap 0}
        [:> Title {:order 1}
        "Source added!"]
        [:> Text {:color :dimmed}
         "Now, configure the actions for your source"]]
       [:> Paper
        [:> Stack
         [:> Title {:order 4}
          "Finish the configuration"]
         [:> Stack
          [:> Title {:order 5}
           "In the next you will:"]
          [:> Text
           "• Define actions for everything your source sends to Duckt"]
          [:> Text
           "• Get the authorization header and how to use it to send data to Duckt"]]
         [:> Divider]
         [:> Group {:justify :right}
          [:> Button
           {:onClick #(rf/dispatch [:navigate
                                    {:handler :source-config
                                     :params {:source-id (:id @new-source-info)}}])}
           "Finish configuring your source"]]]]])))
