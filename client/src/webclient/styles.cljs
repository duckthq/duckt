(ns webclient.styles
  (:require
    [garden.core :refer [css]]))

(def anchor
  [:.anchor {:color "#368951"}])
(def sidebar
  [:.sidebar {:position "fixed"
              :top "0"
              :left "0"
              :width "100%"
              :max-width "250px"
              :height "100vh"}
   [:.sidebar-content {:height "100vh"
                       :width "100%"}]])
(def topbar
  [:.topbar {:position "sticky"
             :top "0"
             :background "var(--mantine-color-white)"
             :border-bottom "1px solid var(--mantine-color-gray-2)"}])

(defn mount-style
  "Mount the style-element into the header with `style-text`, ensuring this is the only `<style>` in the doc"
  [style-text]
  (let [head (or (.-head js/document)
                 (aget (.getElementsByTagName js/document "head") 0))
        style-el (doto (.createElement js/document "style")
                   (-> .-type (set! "text/css"))
                   (.appendChild (.createTextNode js/document style-text)))]
    (.appendChild head style-el)))

(defn build-styles []
  (let [styles (css [sidebar
                     topbar
                     anchor])]
    (mount-style styles)))
