(ns webclient.proxies.new-proxy-success
  (:require
    [reagent.core :as r]
    [re-frame.core :as rf]
    ["@mantine/core" :refer [Stack Group Paper Code]]
    ["@tabler/icons-react" :refer [IconBrandDocker IconBrandGithub]]
    ["js-confetti" :as JSConfetti]
    [webclient.components.h :as h]))

(defn- docker-installation []
  [:div "Docker"])

(defn- from-source-installation []
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
                         {:label "From source"
                          :icon IconBrandGithub
                          :value "from-source"}]
            tabs-panels [{:label "Docker"
                          :component docker-installation}
                         {:label "From source"
                          :component from-source-installation}]]
      (.addConfetti js-confetti
                    (clj->js {:confettiRadius 2
                              :confettiColors ["#999" "#ddd" "#aaa"]
                              :confettiNumber 3000}))
      [:> Stack {:p :md}
       [h/page-title
        "Set up your proxy"
        "Deploy your proxy with the following information."]
       ;; this information is lost when the page is reloaded
       [:> Stack
        [h/h3 "Using Docker"]
        [h/h5 "Pull the image"]
        [:> Paper {:withBorder true
                   :p :md}
         [:> Group
          [:> IconBrandDocker {:size 20
                               :stroke 1.5}]
          [:> Code "docker pull duckthq/proxy"]]]
        [h/h5 "Run the container"]
        [:> Paper {:withBorder true
                   :p :md}
         [:> Group
          [:> Code
           (str "docker run --rm -it -p 4445:4445"
                " -e DUCKT_SERVER_URL=" "URL"
                " -e PROXY_TOKEN=" proxy-key " duckthq/proxy")]]]
        ]]))))
