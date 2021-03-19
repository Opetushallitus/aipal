(ns arvo.auth.user-rights
  (:require [aipal.infra.kayttaja :as kayttaja :refer [*kayttaja*]]
            [clojure.core.match :refer [match]]
            [arvo.db.core :refer [*db*] :as db]
            [clojure.tools.logging :as log]))

(def role->rights
  {"YLLAPITAJA" [:yllapitaja :kysymysryhma :kyselypohja :kysely :kyselykerta :vastaajatunnus :katselu]
   "VASTUUKAYTTAJA" [:kysymysryhma :kyselypohja :kysely :kyselykerta :vastaajatunnus :katselu]
   "KYSELYKERTAKAYTTAJA" [:kyselykerta :vastaajatunnus :katselu]
   "KAYTTAJA" [:vastaajatunnus :katselu]
   "KATSELIJA" [:katselu]})

(defn valtakunnallinen-organization [fn context]
  (let [data (fn context)]
    (if (:valtakunnallinen data)
      (-> *kayttaja* :aktiivinen-rooli :organisaatio)
      (:koulutustoimija data))))

(defn kyselypohja->organization [context]
  (valtakunnallinen-organization db/hae-kyselypohja context))

(defn kysymysryhma->organization [context]
  (valtakunnallinen-organization db/hae-kysymysryhma context))

(defn context->organization [context]
  (match [context]
         [{:kyselypohjaid _}] (kyselypohja->organization context)
         [{:kyselykertaid _}] (:koulutustoimija (db/hae-kyselykerran-organisaatio context))
         [{:kyselyid _}] (:koulutustoimija (db/hae-kysely context))
         [{:kysymysryhmaid _}] (kysymysryhma->organization context)
         :else (-> *kayttaja* :aktiivinen-rooli :organisaatio)))

(defn check-right [right context]
  (let [organization (context->organization context)
        active-role (:aktiivinen-rooli *kayttaja*)
        rights (when (= organization (:organisaatio active-role))
                 (get role->rights (:rooli active-role)))]
    (boolean (some #{right} rights))))

(defmacro authorize [right context & body]
  `(do
     (if (~check-right ~right ~context)
       (do
         ~@body)
       (do
         (log/error "Käyttöoikeudet eivät riitä. Toiminto estetty.")
         (throw (ex-info "Käyttöoikeudet eivät riitä." {:cause :kayttooikeudet}))))))
