(ns webclient.proxies.new-proxy-success
  (:require
    [reagent.core :as r]
    [re-frame.core :as rf]
    ["@mantine/core" :refer [Stack Group Paper Code Anchor]]
    ["@tabler/icons-react" :refer [IconBrandDocker IconBrandGithubFilled]]
    ["js-confetti" :as JSConfetti]
    [webclient.components.h :as h]
    [webclient.components.ui.anchor :as anchor]
    [webclient.components.ui.text :as text]))

(defn panel []
  (let [new-proxy-info (rf/subscribe [:proxies->new-proxy-info])
        js-confetti (new JSConfetti)]
    (println :new-proxy-info @new-proxy-info)
    (fn []
      (let [proxy-key (:proxy-key @new-proxy-info)]
      (.addConfetti js-confetti
                    (clj->js {:confettiRadius 2
                              :confettiColors ["#999" "#ddd" "#aaa"]
                              :confettiNumber 3000}))
      [:> Stack {:p :md}
       [h/page-title
        "Set up your proxy"
        "Deploy your proxy with the following information."]
       [:> Stack
        [h/h3 "Using Docker"]
        [h/h5 "Pull the image"]
        [:> Paper {:withBorder true
                   :p :md}
         [:> Group
          [:> IconBrandDocker {:size 24
                               :color "var(--mantine-color-blue-7)"
                               :stroke 1}]
          [:> Code
           "docker pull duckthq/proxy"]]]
        [h/h5 "Run the container"]
        [:> Paper {:withBorder true
                   :p :md}
         [:> Group
          [:> Code
           (str "docker run --rm -it -p 4445:4445"
                " -e DUCKT_SERVER_URL=" "URL"
                " -e PROXY_TOKEN=" proxy-key " duckthq/proxy")]]]
        ]
       [:> Stack
        [h/h3 "Build from source"]
        [:> Group
         [anchor/Dark {:href "https://github.com/duckthq/duckt/tree/main/proxy"
                       :target "_blank"
                       :inherit true}
          [:> Paper {:withBorder true
                     :p :md}
           [:> Stack
            [:> Group {:gap :xs}
             [:> IconBrandGithubFilled {:size 20}]
             [h/h5 "GitHub"]]
            [text/Base
             "Visit our GitHub repository to build the proxy from source."]]]]]]]))))
