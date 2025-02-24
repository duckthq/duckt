(ns server.middlewares.context
  (:require
    [taoensso.telemere :as t]
    [server.models.users :as users-model]))

(defn user [claims]
  (t/log! :debug (str "Building user context"))
  (users-model/build-user-context (:sub claims)))
