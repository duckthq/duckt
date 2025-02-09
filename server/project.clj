(defproject mainframe "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/core.async "1.6.681"]
                 [environ "1.2.0"]
                 [buddy/buddy-core "1.11.423"]
                 [buddy/buddy-hashers "2.0.167"]
                 [buddy/buddy-sign "3.5.351"]
                 [cider/cider-nrepl "0.42.1"]
                 ;; db
                 [org.postgresql/postgresql "42.7.5"]
                 [com.github.igrishaev/pg2-core "0.1.17"]
                 [com.github.igrishaev/pg2-honey "0.1.17"]
                 [com.github.igrishaev/pg2-migration "0.1.17"]
                 ;; end db
                 [com.taoensso/telemere "1.0.0-SNAPSHOT"]
                 ;; http stuff
                 [http-kit/http-kit "2.8.0"]
                 [cheshire "5.13.0"]
                 [compojure "1.7.1"]
                 [ring/ring-defaults "0.5.0"]
                 [ring/ring-core "1.9.6"]
                 [jumblerg/ring-cors "3.0.0"]
                 [ring/ring-json "0.5.1"]
                 [ring/ring-jetty-adapter "1.9.6"]]
  :plugins [[lein-environ "1.2.0"]]
  :main ^:skip-aot server.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
