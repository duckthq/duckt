(ns webclient.home.time-chart
  (:require
    [reagent.core :as r]
    [re-frame.core :as rf]

    ["@radix-ui/themes" :refer [Grid Card Inset Box]]
    ["chart.js/auto" :as Chart]
    [webclient.components.chart :as chart]))

(def data [{:name "Page A" :uv 4000 :pv 2400 :amt 2400}
           {:name "Page B" :uv 3000 :pv 1398 :amt 2210}
           {:name "Page C" :uv 2000 :pv 9800 :amt 2290}
           {:name "Page D" :uv 2780 :pv 3908 :amt 2000}
           {:name "Page E" :uv 1890 :pv 4800 :amt 2181}
           {:name "Page F" :uv 2390 :pv 3800 :amt 2500}
           {:name "Page G" :uv 3490 :pv 4300 :amt 2100}])

(def colors
  {:light {:gray "#f7fafc"}
   :dark {:gray "#1a202c"}})

(defn main []
  (let [theme (rf/subscribe [:theme])]
    (fn []
      (let [chart-config {:type "line"
                          :data {:labels (map :name data)
                                 :datasets [{:label "UV"
                                             :data (map :uv data)
                                             :borderColor "#0588f0"
                                             :borderWidth 2}]}}]
        [:> Card
         [chart/main {:id "time-chart-container"
                      :config chart-config}]]))))
