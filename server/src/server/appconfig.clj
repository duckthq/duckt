(ns server.appconfig
  (:require
    [clojure.string :as string]))

(def port
  (parse-long (if (or (not (System/getenv "PORT"))
               (string/blank? (System/getenv "PORT")))
         4444
         (System/getenv "PORT"))))

(def jwt-secret-key
  (or (System/getenv "JWT_SECRET_KEY") "secret"))

(def postgres
  {:host (or (System/getenv "DB_HOST") "localhost")
   :port (parse-long (if (or (not (System/getenv "DB_PORT"))
                             (string/blank? (System/getenv "DB_PORT")))
                       5432
                       (System/getenv "DB_PORT")))
   :user (or (System/getenv "DB_USER") "duckt")
   :password (or (System/getenv "DB_PASSWORD") "duckt")
   :database (or (System/getenv "DB_NAME") "duckt")})
(println :postgres postgres)
