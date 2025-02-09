(ns server.api.auth
  (:require
    [cheshire.core :refer [generate-string]]
    [buddy.hashers :as buddy]
    [ring.util.response :refer [response]]
    [buddy.sign.jwt :as jwt]
    [taoensso.telemere :as t]
    [server.appconfig :as appconfig]
    [server.models.users :as users]))

(defn generate-jwt-token [email]
  (let [claims {:sub email
                :exp (+ (System/currentTimeMillis) 3600000)
                :iss "mainframe"}
        token (jwt/sign claims appconfig/jwt-secret-key)]
    token))

(defn signup [req]
  (t/log! :debug "Signing up")
  (let [user (:body req)
        existing-user (users/get-one-by-email (:email user))]
    (if (> (count existing-user) 0)
      (generate-string {:error "User already exists"})
      (let [res (users/register-new-user {:email (:email user)
                                          :fullname (:fullname user)
                                          :password_hash (buddy/derive (:password user))
                                          :username (:email user)})
            claims {:sub (:email user)
                    :exp (+ (System/currentTimeMillis) 3600000)
                    :iss "mainframe"}
            token (jwt/sign claims appconfig/jwt-secret-key)]
        (-> (response {:data res})
            (assoc :headers {"Authorization" token})
            (assoc :cookies {"token" {:value token}})
            (assoc :status 201))))))

(defn login [req]
  (t/log! :debug "Logging in")
  (let [{:keys [email password]} (:body req)]
    (if-let [user (users/get-one-by-email email)]
      (let [_ (println :user user)
            verify (buddy/verify password (:password_hash user))]
        (if (:valid verify)
          (let [token (generate-jwt-token email)]
            ;; TODO: remove token from body
            (-> (response {:status "ok"})
                (assoc :headers {"Authorization" token})
                (assoc :cookies {"token" {:value token}})
                (assoc :status 200)))
          (-> (response {:error "Invalid credentials"})
              (assoc :status 401))))
      (-> (response {:error "Invalid credentials"})
          (assoc :status 401)))))

(defn logout [_]
  (t/log! :debug "Logging out")
  (-> (response {:status "ok"})
      (assoc :cookies {"token" {:value ""}})
      (assoc :status 200)))
