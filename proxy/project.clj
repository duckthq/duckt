(defproject proxy "0.0.1-SNAPSHOT"
  :description ""
  :url "https://duckt.dev"
  :license {:name "AGPL-3.0"
            :url "https://www.gnu.org/licenses/agpl-3.0.en.html"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [cider/cider-nrepl "0.42.1"]
                 [aleph "0.8.2"]
                 [manifold "0.4.3"]
                 [org.clojure/core.async "1.6.681"]
                 [com.taoensso/telemere "1.0.0-SNAPSHOT"]
                 [cheshire "5.13.0"]
                 [clj-http "3.13.0"]
                 ]
  :main proxy.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
