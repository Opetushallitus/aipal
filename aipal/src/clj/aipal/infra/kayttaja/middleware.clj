(ns aipal.infra.kayttaja.middleware
  (:require [aipal.infra.kayttaja.vaihto :refer [with-kayttaja]]))

(defn wrap-kayttaja [handler]
  (fn [request]
    ;; CAS-middleware lisää käyttäjätunnuksen :username-avaimen alle
    (let [uid (:username request)
          impersonoitu-oid (get-in request [:session :impersonoitu-oid])]
      (try
        (with-kayttaja uid impersonoitu-oid
          (handler request))
        (catch IllegalStateException _
          {:headers {"Content-Type" "text/plain;charset=utf-8"}
           :status 403
           :body (str "Käyttäjällä " uid " ei ole voimassaolevaa Aipal-käyttöoikeutta.")})))))
