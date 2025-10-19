(ns proxy.http
  (:require [aleph.http :as http]
            [manifold.stream :as s]
            [manifold.deferred :as d]))

(defn proxy-handler [backend-url]
  (fn [{:keys [request-method uri headers body websocket?] :as req}]
    (if websocket?
      (d/chain
        (http/websocket-client (str backend-url uri))
        (fn [backend-ws]
          (let [client-ws @(http/websocket-connection req)]
            ;; Bidirectional pipe
            (s/connect client-ws backend-ws)
            (s/connect backend-ws client-ws)
            nil)))

      (d/chain
        (http/request
          {:method request-method
           :url (str backend-url uri)
           :headers (dissoc headers "host")
           :body body
           :throw-exceptions false})
        (fn [{:keys [status headers body]}]
          {:status status
           :headers headers
           :body body})))))
