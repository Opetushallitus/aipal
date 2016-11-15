(ns aipal.compojure-util
  (:require compojure.api.meta
            [oph.compojure-util :as cu]
            [aipal.toimiala.kayttajaoikeudet :as ko]))

(defmethod compojure.api.meta/restructure-param :kayttooikeus
  [_ kayttooikeus_spec {:keys [body] :as acc}]
  ; Käyttöoikeuslaajennos compojure-apin rajapintoihin. Esim:
  ;
  ;  :kayttooikeus :jasenesitys-poisto
  ;  :kayttooikeus [:jasenesitys-poisto jasenyysid]

  (let [[kayttooikeus konteksti] (if (vector? kayttooikeus_spec) kayttooikeus_spec [kayttooikeus_spec])]
    (-> acc
        (assoc-in [:swagger :description] (str "Käyttöoikeus " kayttooikeus " , konteksti: " (or konteksti "N/A")))
        (assoc :body [`(cu/autorisoitu-transaktio ~ko/toiminnot ~kayttooikeus ~konteksti ~@body)]))))