(ns arvo.rest-api.automaattitunnus
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-status :as status]
            [ring.util.http-response :as response]
            [arvo.util :refer [on-validation-error]]
            [arvo.schema.automaattitunnus :refer :all]
            [clojure.tools.logging :as log]
            [arvo.db.core :refer [*db*] :as db]
            [arvo.util :refer [api-response]]
            [aipal.asetukset :refer [asetukset]]
            [clj-time.format :as f]
            [schema.core :as s]
            [arvo.service.vastaajatunnus :as vt]))

(defn handle-error
  ([error request-id]
   (log/error "Virhe vastaajatunnuksen luonnissa: "
              (if request-id (str "request-id " request-id " - ") "")
              (:msg error))
   (response/not-found error))
  ([error]
   (handle-error error nil)))

(defn vastauslinkki-response [luotu-tunnus request-id]
  (if (:tunnus luotu-tunnus)
    (api-response {:kysely_linkki (str (:vastaus-base-url @asetukset)"/"(:tunnus luotu-tunnus))
                   :voimassa_loppupvm (f/unparse (f/formatters :date)(:voimassa_loppupvm luotu-tunnus))})
    (handle-error (:error luotu-tunnus) request-id)))

(defn vastaajatunnus-response [luotu-tunnus request-id]
  (if (:tunnus luotu-tunnus)
    (api-response {:tunnus (:tunnus luotu-tunnus)
                   :voimassa_loppupvm (f/unparse (f/formatters :date)(:voimassa_loppupvm luotu-tunnus))})
    (handle-error (:error luotu-tunnus) request-id)))

(defn nippulinkki-response [massatunnus]
  (api-response {:nippulinkki (str (:vastaus-base-url @asetukset)"/n/"(:tunniste massatunnus))
                 :voimassa_loppupvm (f/unparse (f/formatters :date) (:voimassa_loppupvm massatunnus))}))

(defn kyselyynohjaus-response [luotu-tunnus]
  (if {:tunnus luotu-tunnus}
    (api-response {:tunnus (:tunnus luotu-tunnus)})
    (handle-error {:error luotu-tunnus})))

(defroutes kyselyynohjaus-v1
  (POST "/" []
    :body [avopdata s/Any]
    (try
      (let [vastaajatunnus (vt/lisaa-kyselyynohjaus! avopdata)]
        (kyselyynohjaus-response vastaajatunnus))
      (catch java.lang.AssertionError e1
        (log/error e1 "Mandatory fields missing")
        (on-validation-error (format "Mandatory fields are missing or not found")))
      (catch Exception e2
        (log/error e2 "Unexpected error")
        (on-validation-error (format "Unexpected error: %s" (.getMessage e2))))))
  (POST "/rekry" []
    :body [rekrydata s/Any]
    (try
      (let [vastaajatunnus (vt/lisaa-rekry-tunnus! rekrydata)]
        (kyselyynohjaus-response vastaajatunnus))
      (catch java.lang.AssertionError e1
        (log/error e1 "Mandatory fields missing")
        (on-validation-error (format "Mandatory fields are missing or not found")))
      (catch Exception e2
        (log/error e2 "Unexpected error")
        (on-validation-error (format "Unexpected error: %s" (.getMessage e2)))))))

(defonce sallitut-metatiedot [:tila])

(defn poista-vastaajatunnus [tunnus]
  (let [status (db/vastaajatunnus-status {:tunnus tunnus})]
    (if-not (:vastattu status)
      (do (db/poista-vastaajatunnus! {:vastaajatunnusid (:vastaajatunnusid status)})
          (api-response "Tunnus poistettu"))
      (response/not-found "Tunnuksella on jo vastauksia"))))

(defroutes ehoks-v1
  (POST "/" []
    :body [data Amispalaute-tunnus]
    :responses {status/ok {:schema {:kysely_linkki s/Str :voimassa_loppupvm org.joda.time.DateTime}}
                status/not-found {:schema {:ei-kyselykertaa s/Any} :description "Kyselykertaa ei ole olemassa"}}
    :summary "Kyselylinkin luominen"
    :description (str "Päivämäärät ovat ISO-formaatin mukaisia. Suorituskieli on fi, sv tai en. Tutkintotunnus
        on opintopolun koulutus koodiston 6 numeroinen koodi.")
    (let [tunnus (vt/lisaa-amispalaute-tunnus! data)]
      (vastauslinkki-response tunnus (:request_id data))))
  (PATCH "/:tunnus/metatiedot" []
    :path-params [tunnus :- s/Str]
    :body [metatiedot Vastaajatunnus-metatiedot]
    :responses {status/ok {:schema Vastaajatunnus-metatiedot}
                status/not-found {:schema s/Str :description "Ei vastaajatunnusta integraatiokäyttäjälle"}}
    :summary "Metatietojen päivitys"
    :description "Päivitä vastaajatunnuksen valitut metatiedot. Ei voi käyttää metatietokentän poistamiseen."
    (let [paivitettavat-metatiedot (select-keys metatiedot sallitut-metatiedot)
          rivia-paivitetty (vt/paivita-metatiedot tunnus metatiedot)]
      (if (not= rivia-paivitetty 0)
        (api-response paivitettavat-metatiedot)
        (response/not-found "Ei vastaajatunnusta integraatiokäyttäjälle"))))
  (GET "/status/:tunnus" []
    :path-params [tunnus :- s/Str]
    :return (s/maybe {:tunnus s/Str :voimassa_loppupvm org.joda.time.DateTime :vastattu s/Bool})
    :summary "Kyselylinkin tila"
    (let [status (db/vastaajatunnus-status {:tunnus tunnus})]
      (api-response (dissoc status :vastaajatunnusid))))
  (DELETE "/:tunnus" []
    :path-params [tunnus :- s/Str]
    :responses {status/ok {:schema s/Str :description "Tunnus poistettu"}
                status/not-found {:schema s/Str :description "Tunnuksella on jo vastauksia"}}
    :summary "Poista kyselylinkki"
    (poista-vastaajatunnus tunnus)))

(defroutes tyoelamapalaute-v1
  (POST "/vastaajatunnus" []
    :body [data Tyoelamapalaute-tunnus]
    :responses {status/ok {:schema {:tunnus s/Str :voimassa_loppupvm org.joda.time.DateTime}}
                status/not-found {:schema {:error s/Str :msg s/Str}}}
    :summary "Yksittäisen vastaajatunnuksen luominen"
    (let [luotu-tunnus (vt/lisaa-tyoelamapalaute-tunnus! data)]
      (vastaajatunnus-response luotu-tunnus (:request-id data))))
  (DELETE "/vastaajatunnus/:tunnus" []
    :path-params [tunnus :- s/Str]
    :responses {status/ok {:schema s/Str :description "Tunnus poistettu"}
                status/not-found {:schema s/Str :description "Tunnuksella on jo vastauksia"}}
    :summary "Poista vastaajatunnus"
    (poista-vastaajatunnus tunnus))
  (POST "/nippu" []
    :body [data Nippulinkki]
    :responses {status/ok {:schema {:nippulinkki s/Str :voimassa_loppupvm org.joda.time.DateTime}}
                status/not-found {:schema {:errors [s/Str]}}}
    :summary "Yksittäisten linkkien niputus yhdeksi nipputunnukseksi"
    (let [nippu (vt/niputa-tunnukset! data)]
      (if-not (:errors nippu)
        (nippulinkki-response nippu)
        (do
          (println "Virhe nipun luonnissa: " (:errors nippu))
          (response/not-found {:errors (:errors nippu)})))))
  (DELETE "/nippu/:tunniste" []
    :path-params [tunniste :- s/Str]
    :summary "Poista nippu"
    (let [result (vt/poista-nippu tunniste)]
      (if-not (:error result)
        (api-response "Tunnus poistettu")
        (response/not-found (:error result))))))
