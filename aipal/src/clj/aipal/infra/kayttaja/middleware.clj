(ns aipal.infra.kayttaja.middleware
  (:require [aipal.infra.kayttaja.vaihto :refer [with-kayttaja]]
            [clojure.tools.logging :as log]))

(def unauthorized-virheilmoitus
   (str
      "ARVO-palautejärjestelmän käyttö edellyttää käyttöoikeuksia, jotka sinun käyttäjätunnukseltasi puuttuvat. "
      "Tarvittaessa ota yhteyttä oman organisaatiosi ARVO-koulutustoimijan pääkäyttäjään tai OKM:n ARVO-neuvontaan (arvo@csc.fi).\n\n"
      "Användningen av responssystemet ARVO förutsätter användarrättigheter. I din användarkod finns ej användarrättigheter. "
      "Kontakta vid behov huvudanvändaren för ARVO i din egen organisation eller UBS ARVO-rådgivning (arvo@csc.fi). "))

(defn wrap-kayttaja
  ([handler paasykielletty-virheilmoitus]
   (fn [request]
      ;; CAS-middleware lisää käyttäjätunnuksen :username-avaimen alle
     (let [uid (:username request)
           impersonoitu-oid (get-in request [:session :impersonoitu-oid])
           vaihdettu-organisaatio (get-in request [:session :vaihdettu-organisaatio])
           rooli (get-in request [:session :rooli])]
       (try
         (with-kayttaja uid {:kayttaja impersonoitu-oid :organisaatio vaihdettu-organisaatio} rooli
           (handler request))
         (catch IllegalStateException _
           {:headers {"Content-Type" "text/plain;charset=utf-8"}
            :status 403
            :body paasykielletty-virheilmoitus})))))
  ([handler]
   (wrap-kayttaja handler unauthorized-virheilmoitus)))
