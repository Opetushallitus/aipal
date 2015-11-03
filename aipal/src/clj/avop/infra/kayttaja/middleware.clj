(ns avop.infra.kayttaja.middleware
  (:require [aipal.infra.kayttaja.middleware :refer [wrap-kayttaja]]))

(defn wrap-avop-kayttaja [handler]
  (let [paasykielletty-virheilmoitus 
        (str
          "AVOP-palautejärjestelmän käyttö edellyttää käyttöoikeuksia, jotka sinun käyttäjätunnukseltasi puuttuvat. "
          "Tarvittaessa ota yhteyttä oman organisaatiosi AVOP-koulutustoimijan pääkäyttäjään tai OKM:n AVOP-neuvontaan (avop@postit.csc.fi).\n\n"
          "Användningen av responssystemet AVOP förutsätter användarrättigheter. I din användarkod finns ej användarrättigheter. "
          "Kontakta vid behov huvudanvändaren för AVOP i din egen organisation eller UBS AIPAL-rådgivning (avop@postit.csc.fi). ")]
    (wrap-kayttaja handler paasykielletty-virheilmoitus)))
