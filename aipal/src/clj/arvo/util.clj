(ns arvo.util
  (:require [aipal.asetukset :refer [asetukset]]))

(defn in? [coll elem]
  (some #(= elem %) coll))

(defn format-url [base params]
  (str base "?"(->> params
                    (map #(str (name (first %))"=" (second %)))
                    (interpose "&")
                    (apply str))))

(defn api-response [body]
  {:status 200
   :body body
   :headers {"Content-Type" "application/json; charset=utf-8"}})

(defn paginated-response [data key page-length api-url params]
  (let [next-id (when (= page-length (count data)) (-> data last key))
        next-url (format-url (str (-> @asetukset :server :base-url) api-url) (merge params {:since next-id}))]
    (if (some? data)
      (api-response {:data data
                     :pagination {:next_url (if next-id next-url "null")}})
      {:status 404})))