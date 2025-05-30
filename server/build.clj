(ns build
  (:require [clojure.tools.build.api :as b]))

(def lib 'duckt-server)
(def version "0.0.1-SNAPSHOT")
(def main 'server.core)
(def class-dir "target/classes")
(def uber-file "target/duckt-server-standalone.jar")

(def basis (delay (b/create-basis {:project "deps.edn"})))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis @basis
                  :ns-compile '[server.core]
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis @basis
           :main main}))
