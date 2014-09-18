(ns aipal.rest-api.rest-util
  (:require [peridot.core :as peridot]
            [clj-time.core :as time]
            [cheshire.core :as cheshire]

            [oph.common.infra.i18n :as i18n]
            [aipal.palvelin :as palvelin]
            [aipal.asetukset :refer [hae-asetukset oletusasetukset]]
            [aipal.integraatio.sql.korma :as korma]
            [aipal.toimiala.kayttajaroolit :refer [kayttajaroolit]]
            [aipal.infra.kayttaja.vaihto :refer [with-kayttaja]]
            [aipal.infra.kayttaja.vakiot :refer [default-test-user-uid]]

            [aipal.sql.test-util :refer :all]
            [aipal.sql.test-data-util :refer :all]))

(defn with-auth-user [f]
  (with-kayttaja default-test-user-uid nil
    (binding [i18n/*locale* testi-locale]
      (f))))

(defn mock-request [app url method params]
  (with-auth-user
    #(peridot/request app url
      :request-method method
      :headers {"x-xsrf-token" "token"}
      :cookies {"XSRF-TOKEN" {:value "token"}}
      :params params)))

(defn mock-request-uid [app url method uid params]
  (peridot/request app url
    :request-method method
    :headers {"x-xsrf-token" "token"
              "uid" uid}
    :cookies {"XSRF-TOKEN" {:value "token"}}
    :params params))

(defn rest-kutsu
  "Tekee yksinkertaisen simuloidun rest-kutsun. Peridot-sessio suljetaan
lopuksi. Soveltuu yksinkertaisiin testitapauksiin."
  [url method params]
  (let [asetukset (-> oletusasetukset
                    (assoc-in [:cas-auth-server :enabled] false)
                    (assoc :development-mode true))
        _ (alusta-korma! asetukset)
        crout (palvelin/app asetukset)]
    (-> (peridot/session crout)
      (mock-request-uid url method "T-1001" params))))

(defn json-find
  "Etsii haluttua avain-arvo paria vastaavaa osumaa json-rakenteesta.
JSON-rakenne on joko map tai lista mappeja."
  [json-str avain arvo]
  (let [s (cheshire/parse-string json-str true)]
    (or (and (map? s)
             (= (get s avain) arvo))
        (some #(= arvo (get % avain)) s))))
