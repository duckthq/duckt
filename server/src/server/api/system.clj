(ns server.api.system
  (:require
    [ring.util.response :refer [response]]
    [taoensso.telemere :as t]))

(defn system-actions [req & _]
  (t/log! :debug "System actions")
  (let [system-actions [{:id "http-request"
                         :name "HTTP Request"
                         :description (str "Send custom HTTP requests to any URL")
                         :icon "IconApi"}
                        {:id "email"
                         :name "Email"
                         :description (str "Send emails to any email address. Powered by Resend.")
                         :icon "IconMail"}]]
    (response {:data system-actions})))
