(ns webclient.components.ui.text
  (:require
    ["@mantine/core" :refer [Text]]))

(defn Base
  ([props text]
   [:> Text
    (merge
      props
      {:size :sm})
    text])
  ([text] [Base {} text]))

(defn Dimmed
  ([props text]
   [:> Text
    (merge
      {:size :sm
       :c :dimmed}
      props)
    text])
  ([text] [Dimmed {} text]))
