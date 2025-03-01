(ns webclient.proxies.new-proxy-success
  (:require
    [reagent.core :as r]
    [re-frame.core :as rf]
    ["@mantine/core" :refer [Stack Group Paper Code Tabs Accordion Box Table Timeline Text]]
    ["@tabler/icons-react" :refer [IconBrandDocker IconExternalLink IconCpu
                                   IconBrandGithubFilled IconDownload IconTerminal]]
    ["js-confetti" :as JSConfetti]
    [webclient.components.h :as h]
    [webclient.components.clipboard :as clipboard]
    [webclient.components.ui.title :as title]
    [webclient.components.ui.text :as text]
    [webclient.components.ui.anchor :as anchor]))

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
               [text/Dimmed "Image:"]
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
               [h/h5 "You may also run the container locally"]
               [:> Paper {:withBorder true
                          :p :md}
                [:> Group {:justify :end}
                 [:> Code
                  (str "docker run --rm -it -p 4445:4445"
                       " -e PROXY_TOKEN=" (:proxy-key proxy-info) " " (:image a))]]]]]]))]])))

(defn bare-metal-installation []
  (fn [proxy-info]
    [:> Stack
     [text/Base "You can download the standalone jar file and run it on any machine with Java installed."]
     [:> Timeline
      [:> Timeline.Item {:title "Download the standalone jar file"
                         :bullet (r/as-element [:> IconBrandGithubFilled {:size 16}])}
       [text/Base
        [:span
         [:span "Download the latest proxy standalone jar file from the "]
         [anchor/Base {:href "https://github.com/duckthq/duckt/releases"
                       :target "_blank"}
          [:span
           [:span "Duckt GitHub releases page."]
           [:> IconExternalLink {:size 14}]]]]]]
      [:> Timeline.Item {:title "Install Java"
                         :bullet (r/as-element [:> IconDownload {:size 16}])}
       [text/Base [:span [:span "Make sure you have Java installed on your machine. "]
                   [anchor/Base
                    {:href "https://www.java.com/en/download/help/download_options.html"
                     :target "_blank"}
                    "You can download Java from the official website."]]]]
      [:> Timeline.Item {:title "Run the jar file"
                         :bullet (r/as-element [:> IconTerminal {:size 16}])}
       [text/Base "Run the jar file replacing <version> with the actual downloaded version"]
       [:> Paper {:withBorder true
                  :p :md}
        [:> Group
         [:> Code (str
                    "PROXY_TOKEN=" (:proxy-key proxy-info) " "
                    "java -jar proxy-standalone-<version>.jar")]]]]]]))

(defn panel []
  (let [new-proxy-info (rf/subscribe [:proxies->new-proxy-info])
        js-confetti (new JSConfetti)]
    (fn []
      (.addConfetti js-confetti
                    (clj->js {:confettiRadius 2
                              :confettiColors ["#999" "#ddd" "#aaa"]
                              :confettiNumber 3000}))
      [:> Stack {:p :md
                 :gap :xl}
       [h/page-title
        "Set up your proxy"
        "Deploy your proxy with the following information."]
       [:> Accordion {:variant :contained
                      :defaultValue "Docker"
                      :radius :lg}
        [:> Accordion.Item {:value "Docker"}
         [:> Accordion.Control {:icon (r/as-element
                                        [:> IconBrandDocker
                                         {:size 24
                                          :stroke 1}])}
          [title/h3 "Docker"]]
         [:> Accordion.Panel [docker-installation @new-proxy-info]]]

        [:> Accordion.Item {:value "Bare Metal"}
         [:> Accordion.Control {:icon (r/as-element
                                        [:> IconCpu
                                         {:size 24
                                          :stroke 1}])}
          [title/h3 "Bare Metal"]]
         [:> Accordion.Panel [bare-metal-installation @new-proxy-info]]]]])))
