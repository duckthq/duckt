(ns proxy.appconfig)

(def log-level (or (System/getenv "LOG_LEVEL") "info"))

(def http-proxy-port
  (or (System/getenv "PROXY_PORT") "4445"))
