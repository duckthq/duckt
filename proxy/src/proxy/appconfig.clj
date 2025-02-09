(ns proxy.appconfig)

(def log-level (or (System/getenv "LOG_LEVEL") "info"))

(def proxy-port
  (or (System/getenv "PROXY_PORT") "4445"))

(def duckt-server-url
  (System/getenv "DUCKT_SERVER_URL"))

;; token format: base64 encoded string of the form "<version>:<proxy-uuid>:<secret>"
(def proxy-token
  (System/getenv "PROXY_TOKEN"))
