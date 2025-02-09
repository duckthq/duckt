(ns server.api.users
  (:require
    [ring.util.response :refer [response]]
    [taoensso.telemere :as t]))

(defn get-users [req]
  (t/log! :debug "Getting users")
  (response {:status "ok"
             :data "users"}))
