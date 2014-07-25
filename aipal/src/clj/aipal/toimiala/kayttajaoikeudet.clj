(ns aipal.toimiala.kayttajaoikeudet
  "https://knowledge.solita.fi/pages/viewpage.action?pageId=61901330"
  (:require 
    [aipal.toimiala.kayttajaroolit :refer :all]))

(def ^:dynamic *current-user-authmap*)
(def ^:dynamic *impersonoitu-oid* nil)
 