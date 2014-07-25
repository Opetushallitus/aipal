(ns aipal.compojure-util
  (:require [oph.compojure-util :as oph-cjure]
    [aipal.toimiala.kayttajaoikeudet :as ko]
    ))

(defmacro defapi
  "Esittelee rajapinta-funktion sisältäen käyttöoikeuksien tarkastamisen ja tietokanta-transaktion hallinnan."
  [toiminto konteksti-arg http-method path args & body]
  (let [auth-map ko/toiminnot]
    (println "mappi " auth-map)
    `(oph-cjure/defapi  ~auth-map ~toiminto ~konteksti-arg ~http-method ~path ~args ~@body)))
