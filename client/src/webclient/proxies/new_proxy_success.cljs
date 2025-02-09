(ns webclient.proxies.new-proxy-success
  (:require
    [reagent.core :as r]
    [re-frame.core :as rf]
    ["@mantine/core" :refer [Stack Tabs]]
    ["@tabler/icons-react" :refer [IconBrandDocker IconServer]]
    ["js-confetti" :as JSConfetti]
    [webclient.components.h :as h]))

(defn- docker-installation []
  [:div "Docker"])

(defn- bare-metal-installation []
  [:div "Bare metal"])

(defn panel []
  (let [new-proxy-info (rf/subscribe [:proxies->new-proxy-info])
        js-confetti (new JSConfetti)]
    (println :new-proxy-info @new-proxy-info)
    (fn []
      (let [proxy-key (str
                        (or (:version @new-proxy-info)
                            "v1")
                        ":"
                        (:id @new-proxy-info)
                        ":"
                        (:proxy-key @new-proxy-info))
            tabs-values [{:label "Docker"
                          :icon IconBrandDocker
                          :value "docker"}
                         {:label "Bare metal"
                          :icon IconServer
                          :value "bare-metal"}]
            tabs-panels [{:label "Docker"
                          :component docker-installation}
                         {:label "Bare metal"
                          :component bare-metal-installation}]]
      (.addConfetti js-confetti
                    (clj->js {:confettiRadius 2
                              :confettiColors ["#999" "#ddd" "#aaa"]
                              :confettiNumber 3000}))
      [:> Stack {:p :md}
       [h/page-title
        "New proxy created"
        "Deploy your proxy with the following information."]
       ;; this information is lost when the page is reloaded
       [:div "New Proxy key: " proxy-key]
       [:> Tabs {:defaultValue "Docker"}
        [:> Tabs.List
         (doall
           (for [tab tabs-values]
             ^{:key (:label tab)}
             [:> Tabs.Tab
              {:value (:label tab)
               :leftSection (r/as-element [:> (:icon tab) {:size 20
                                                           :stroke 1}])}
              (:label tab)]))]
        (doall
          (for [panel tabs-panels]
            ^{:key (:label panel)}
            [:> Tabs.Panel
             {:value (:label panel)
              :p :sm}
             [(:component panel)]]))]]))))
