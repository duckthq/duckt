(ns server.appconfig)

(def port
  (if (System/getenv "PORT")
    (parse-long (System/getenv "PORT"))
    4444))

(def log-level
  (if (System/getenv "LOG_LEVEL")
    (System/getenv "LOG_LEVEL")
    "info"))

(def invite-only?
  (if (System/getenv "INVITE_ONLY")
    (System/getenv "INVITE_ONLY")
    false))

(def jwt-secret-key
  (or (System/getenv "JWT_SECRET_KEY") "secret"))

(def postgres
  {:host (or (System/getenv "DB_HOST") "localhost")
   :port (if (System/getenv "DB_PORT")
           (parse-long (System/getenv "DB_PORT"))
           5432)
   :user (or (System/getenv "DB_USER") "duckt")
   :password (or (System/getenv "DB_PASSWORD") "duckt")
   :database (or (System/getenv "DB_NAME") "duckt")})
