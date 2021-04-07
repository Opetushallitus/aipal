(ns aipal.arkisto.kayttajaoikeus
  (:require [aipal.arkisto.kayttaja :as kayttaja-arkisto]
            [aipal.infra.kayttaja :refer [*kayttaja*]]
            [aipal.infra.kayttaja.vakiot :refer [integraatio-uid]]
            [arvo.db.core :refer [*db*] :as db]
            [clojure.java.jdbc :as jdbc]
            [clojure.set :as set]
            [aipal.toimiala.kayttajaroolit :refer [kayttooikeus->rooli roolijarjestys]]))

(defn hae-roolit
  ([tx oid]
   (let [kayttooikeudet (db/hae-voimassaolevat-roolit tx {:kayttajaOid oid})]
     (->> kayttooikeudet
          (map #(merge % (kayttooikeus->rooli (:kayttooikeus %)))))))
  ([oid]
   (jdbc/with-db-transaction [tx *db*]
     (hae-roolit tx oid))))

(defn hae-oikeudet
  ([oid]
   (let [kayttaja (kayttaja-arkisto/hae oid)
         roolit (hae-roolit oid)
         laajennettu (db/hae-laajennettu {:koulutustoimijat (map :organisaatio roolit)})]
     (-> kayttaja
         (merge laajennettu)
         (assoc :roolit roolit))))
  ([]
   (hae-oikeudet (:oid *kayttaja*))))

(defn paivita-roolit! [tx k impersonoitu-oid]
  (let [vanhat-roolit (->> (db/hae-roolit tx {:kayttaja (:oid k)})
                           (into #{}))
        poistuneet-roolit (set/difference
                              vanhat-roolit
                              (into #{} (map #(select-keys % [:kayttooikeus :organisaatio]) (:roolit k))))]
    (doseq [r poistuneet-roolit]
      (db/aseta-roolin-tila! tx (merge r {:kayttaja (:oid k) :voimassa false})))
    (doseq [r (:roolit k)]
      (if (contains? vanhat-roolit (select-keys r [:kayttooikeus :organisaatio]))
        (db/aseta-roolin-tila! tx (merge r {:kayttaja (:oid k) :voimassa true}))
        (db/lisaa-rooli! tx (assoc r :kayttaja (:oid k)))))
    (hae-roolit (or impersonoitu-oid (:oid k)))))


(defn paivita-kayttaja! [k impersonoitu-oid]
  (jdbc/with-db-transaction [tx *db*]
    (let [olemassa? (db/hae-kayttaja {:kayttajaOid (:oid k)})]
      (if olemassa?
        (db/paivita-kayttaja! tx {:kayttajaOid (:oid k) :etunimi (:etunimi k) :sukunimi (:sukunimi k)})
        (db/lisaa-kayttaja! tx (assoc k :kayttajaOid (:oid k)))))
    (assoc k :roolit (paivita-roolit! tx k impersonoitu-oid))))
