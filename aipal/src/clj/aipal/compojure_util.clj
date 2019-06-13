(ns aipal.compojure-util
  (:require compojure.api.meta
            [arvo.auth.user-rights :refer [authorize]]))

(defmacro autorisoitu-transaktio
  "Tarkastaa käyttöoikeudet ja hallitsee tietokanta-transaktion"
  [toiminto konteksti & body]
  `(korma.db/transaction
     (authorize ~toiminto ~konteksti ~@body)))


(defmethod compojure.api.meta/restructure-param :kayttooikeus
  [_ kayttooikeus_spec {:keys [body] :as acc}]
  " Käyttöoikeuslaajennos compojure-apin rajapintoihin. Esim:
    :kayttooikeus :jasenesitys-poisto
    :kayttooikeus [:jasenesitys-poisto jasenyysid]"
  (let [[kayttooikeus konteksti] (if (vector? kayttooikeus_spec) kayttooikeus_spec [kayttooikeus_spec nil])]
    (-> acc
        (assoc-in [:swagger :description] (str "Käyttöoikeus " kayttooikeus " , konteksti: " (or konteksti "N/A")))
        (assoc :body [`(autorisoitu-transaktio ~kayttooikeus ~konteksti ~@body)]))))