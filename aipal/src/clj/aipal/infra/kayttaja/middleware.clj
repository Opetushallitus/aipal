(ns aipal.infra.kayttaja.middleware
  (:require [aipal.infra.kayttaja.vaihto :refer [with-kayttaja]]))

(def unauthorized-virheilmoitus
  (str
    "AIPAL-palautejärjestelmän käyttö edellyttää käyttöoikeuksia, jotka sinun käyttäjätunnukseltasi puuttuvat. "
    "Tarvittaessa ota yhteyttä oman organisaatiosi AIPAL-koulutustoimijan pääkäyttäjään tai Opetushallituksen AIPAL-neuvontaan (aipal@oph.fi).\n\n"
    "Användningen av responssystemet AIPAL förutsätter användarrättigheter. I din användarkod finns ej användarrättigheter. "
    "Kontakta vid behov huvudanvändaren för AIPAL i din egen organisation eller Utbildningsstyrelsens AIPAL-rådgivning (aipal@oph.fi). "))

(defn wrap-kayttaja
  ([handler paasykielletty-virheilmoitus]
    (fn [request]
      ;; CAS-middleware lisää käyttäjätunnuksen :username-avaimen alle
      (let [uid (:username request)
            impersonoitu-oid (get-in request [:session :impersonoitu-oid])
            rooli (get-in request [:session :rooli])]
        (try
          (with-kayttaja uid impersonoitu-oid rooli
            (handler request))
          (catch IllegalStateException _
            {:headers {"Content-Type" "text/plain;charset=utf-8"}
             :status 403
             :body paasykielletty-virheilmoitus})))))
  ([handler]
    (wrap-kayttaja handler unauthorized-virheilmoitus)))