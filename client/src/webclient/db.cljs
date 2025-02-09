(ns webclient.db)

(def default-db
  {:theme :light
   :is-mac?  (>= (.indexOf (.toUpperCase (.-platform js/navigator)) "MAC") 0)})
