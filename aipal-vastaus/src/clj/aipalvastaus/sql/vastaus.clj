;; Copyright (c) 2014 The Finnish National Board of Education - Opetushallitus
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

(ns aipalvastaus.sql.vastaus
  (:require [korma.core :as sql]
            [clj-time.core :as time]
            [clj-time.coerce :as time-coerce]))

(defn tallenna!
  [vastaus]
  (sql/insert :vastaus
    (sql/values {:kysymysid (:kysymysid vastaus)
                 :vastaajaid (:vastaajaid vastaus)
                 :vastausaika (time-coerce/to-sql-date (time/today))
                 :vapaateksti (:vapaateksti vastaus)
                 :numerovalinta (:numerovalinta vastaus)
                 :vaihtoehto (:vaihtoehto vastaus)})))
