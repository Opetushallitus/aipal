(ns aipal.infra.kayttaja.middleware
  (:require [aipal.infra.kayttaja.vaihto :refer [with-kayttaja]]))

(def unauthorized-virheilmoitus
   (str
      "AVOP-palautejärjestelmän käyttö edellyttää käyttöoikeuksia, jotka sinun käyttäjätunnukseltasi puuttuvat. "
      "Tarvittaessa ota yhteyttä oman organisaatiosi AVOP-koulutustoimijan pääkäyttäjään tai OKM:n AVOP-neuvontaan (avop@postit.csc.fi).\n\n"
      "Användningen av responssystemet AVOP förutsätter användarrättigheter. I din användarkod finns ej användarrättigheter. "
      "Kontakta vid behov huvudanvändaren för AVOP i din egen organisation eller UBS AVOP-rådgivning (avop@postit.csc.fi). "))

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