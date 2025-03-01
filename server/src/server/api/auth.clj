(ns server.api.auth
  (:require
    [clojure.string :as string]
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

(defn validate-invite [invite-code]
  (if appconfig/invite-only?
    (if-let [invites (System/getenv "INVITE_CODES")]
      (let [codes-list (string/split invites #",")]
        (if (some #(= % invite-code) codes-list)
          true
          false))
      false)
    true))

(defn signup [req]
  (t/log! :debug "Signing up")
  (let [user (:body req)
        existing-user (users/get-one-by-email (:email user))]
    (if (> (count existing-user) 0)
      (generate-string {:error "User already exists"})
      (if-let [_ (validate-invite (:invite_code user))]
        (let [res (users/register-first-workspace-user
                    {:email (:email user)
                     :fullname (:fullname user)
                     :status "active"
                     :password_hash (buddy/derive (:password user))
                     :username (:email user)})
              claims {:sub (:email user)
                      :exp (+ (System/currentTimeMillis) 3600000)
                      :iss "mainframe"}
              token (jwt/sign claims appconfig/jwt-secret-key)]
          (-> (response {:data res})
              (assoc :cookies {"token" {:value token
                                        :same-site :none
                                        :secure true
                                        :http-only true
                                        :path "/"
                                        :max-age 864000}})
              (assoc :status 201)))
        (-> (response {:error "Invalid invite code"})
            (assoc :status 401))))))

(defn login [req]
  (t/log! :debug "Logging in")
  (let [{:keys [email password]} (:body req)]
    (if-let [user (users/get-one-by-email email)]
      (let [verify (buddy/verify password (:password_hash user))]
        (if (:valid verify)
          (let [token (generate-jwt-token email)]
            ;; TODO: remove token from body
            (-> (response {:status "ok"})
                (assoc :cookies {"token" {:value token
                                          :same-site :none
                                          :secure true
                                          :http-only true
                                          :path "/"
                                          :max-age 864000}})
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
