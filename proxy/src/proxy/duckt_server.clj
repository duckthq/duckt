(ns proxy.duckt-server
  (:require [clj-http.client :as http]
            [taoensso.telemere :as t]
            [cheshire.generate :refer [add-encoder]]
            [cheshire.core :as json :refer [generate-string]]
            [proxy.appconfig :as appconfig]))

;; Add encoder for java.time.OffsetDateTime on the responses
(add-encoder java.time.OffsetDateTime cheshire.generate/encode-str)
(add-encoder java.time.Instant cheshire.generate/encode-str)
(add-encoder java.time.ZonedDateTime cheshire.generate/encode-str)

(defn send-request [request-payload {:keys [proxy-id proxy-secret]}]
  (t/log! :debug "Sending request to server")
  (let [body (generate-string request-payload)
        response (http/post
                   (str appconfig/duckt-server-url "/p/requests")
                   {:body body
                    :accept :json
                    :connection-timeout 1000
                    :content-type :json
                    :throw-exceptions false
                    :headers {"proxy-secret" proxy-secret
                              "proxy-id" proxy-id}})]
    (:body response)))

(defn set-alive! [proxy-id proxy-secret]
  (t/log! :info "Setting myself alive...")
  (let [response (http/post
                  (str appconfig/duckt-server-url "/p/alive")
                  {:accept :json
                   :connection-timeout 1000
                   :content-type :json
                   :throw-exceptions false
                   :headers {"proxy-secret" proxy-secret
                             "proxy-id" proxy-id}})]
    (json/parse-string (:body response) true)))
