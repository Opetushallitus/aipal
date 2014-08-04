(ns aipal.arkisto.vastaajatunnus-url-test
    (:require [aipal.arkisto.vastaajatunnus :refer :all])
    (:use clojure.test))

(deftest tunnusten-yksilollisyys []
  ; Z on ainoa mahdollinen joka ei ole jo luotujen tunnusten joukossa 
  (is "Z" (luo-tunnus 1 (set "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXY"))))
