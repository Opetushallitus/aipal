(ns aipal.compojure-util
  (:require compojure.api.meta
            [oph.compojure-util :as cu]
            [korma.db :as db]
            [aipal.toimiala.kayttajaoikeudet :as ko]))

(defmacro autorisoitu-transaktio
  "Tarkastaa käyttöoikeudet ja hallitsee tietokanta-transaktion"
  [auth-map toiminto konteksti & body]
  `(korma.db/transaction
     (cu/autorisoi ~auth-map ~toiminto ~konteksti ~@body)))


(defmethod compojure.api.meta/restructure-param :kayttooikeus
  [_ kayttooikeus_spec {:keys [body] :as acc}]
  " Käyttöoikeuslaajennos compojure-apin rajapintoihin. Esim:
    :kayttooikeus :jasenesitys-poisto
    :kayttooikeus [:jasenesitys-poisto jasenyysid]"
  (let [[kayttooikeus konteksti] (if (vector? kayttooikeus_spec) kayttooikeus_spec [kayttooikeus_spec])]
    (-> acc
        (assoc-in [:swagger :description] (str "Käyttöoikeus " kayttooikeus " , konteksti: " (or konteksti "N/A")))
        (assoc :body [`(autorisoitu-transaktio ~ko/toiminnot ~kayttooikeus ~konteksti ~@body)]))))