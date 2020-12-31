(ns aipal.arkisto.kyselykerta-sql-test
  (:require [clojure.test :refer :all]
            [korma.core :as sql]
            [aipal.arkisto.kyselykerta :refer :all]
            [aipal.integraatio.sql.korma :as taulut]
            [aipal.sql.test-data-util :as test-data]
            [aipal.sql.test-util :refer [tietokanta-fixture] :as test-util]
            [oph.common.util.util :refer [some-value
                                          some-value-with]]))

(use-fixtures :each tietokanta-fixture)

(defn lisaa-kyselykerta-johon-on-vastattu!
  ([kyselykerta]
   (lisaa-kyselykerta-johon-on-vastattu! kyselykerta (test-data/lisaa-kysely!)))
  ([kyselykerta kysely]
   (let [kyselykerta      (test-data/lisaa-kyselykerta! kyselykerta kysely)
         [vastaajatunnus] (test-data/lisaa-vastaajatunnus! {:kohteiden_lkm 1} kyselykerta)
         _                (test-data/lisaa-vastaaja! {:vastannut true} vastaajatunnus)]
     kyselykerta)))

(defn lisaa-kyselykerta-ilman-vastaajia!
  ([kyselykerta]
   (lisaa-kyselykerta-ilman-vastaajia! kyselykerta (test-data/lisaa-kysely!)))
  ([kyselykerta kysely]
   (test-data/lisaa-kyselykerta! kyselykerta kysely)))