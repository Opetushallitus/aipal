;; Copyright (c) 2013 The Finnish National Board of Education - Opetushallitus
;;
;; This program is free software:  Licensed under the EUPL, Version 1.1 or - as
;; soon as they will be approved by the European Commission - subsequent versions
;; of the EUPL (the "Licence");
;;
;; You may not use this work except in compliance with the Licence.
;; You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
;;
;; This program is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; European Union Public Licence for more details.

(ns user
  (:require [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.java.shell :refer [with-sh-dir sh]]
            [clojure.tools.namespace.repl :as nsr]
            [clj-http.client :as hc]
            clojure.core.cache
            schema.core
            stencil.loader))

(schema.core/set-fn-validation! true)

;; Templatejen kakutus pois päältä kehityksen aikana
(stencil.loader/set-cache (clojure.core.cache/ttl-cache-factory {} :ttl 0))

(def frontend-kaannoskomennot ["npm install"
                               "bower install"
                               "grunt build"])

(defn kaanna-frontend []
  (with-sh-dir "frontend"
    (doseq [komento frontend-kaannoskomennot]
      (println "$" komento)
      (let [{:keys [err out]} (sh "bash" "-l" "-c" komento)]
        (println err out)))))

(defonce ^:private palvelin (atom nil))

(defn ^:private repl-asetukset
  "Muutetaan oletusasetuksia siten että saadaan järkevät asetukset kehitystyötä varten"
  []
  (->
    @(ns-resolve 'aipal.asetukset 'oletusasetukset)
    (assoc :development-mode true
           :raportointi-minimivastaajat -1
           :cas-auth-server {:url "https://virkailija.testiopintopolku.fi/cas"
                             :unsafe-https true
                             :enabled true})))

(defn eraaja-organisaatiot! []
  (require 'aipal.infra.eraajo.organisaatiot)
  ((ns-resolve 'aipal.infra.eraajo.organisaatiot 'paivita-organisaatiot!)
   {"url" (((repl-asetukset) :organisaatiopalvelu) :url)}))

(defn start []
  {:pre [(not @palvelin)]
   :post [@palvelin]}
  (require 'aipal.palvelin)
  (reset! palvelin ((ns-resolve 'aipal.palvelin 'kaynnista!) @(ns-resolve 'aipal.asetukset 'oletusasetukset))))

(defn stop []
  {:pre [@palvelin]
   :post [(not @palvelin)]}
  ((ns-resolve 'aipal.palvelin 'sammuta) @palvelin)
  (reset! palvelin nil))

(defn uudelleenkaynnista! []
  (when @palvelin
    (stop))
  (nsr/refresh :after 'user/kaynnista!))

(defmacro with-testikayttaja [& body]
  `(aipal.infra.kayttaja.vaihto/with-kayttaja aipal.infra.kayttaja.vakiot/default-test-user-uid nil nil
     ~@body))
