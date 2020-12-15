(ns arvo.util
  (:require [aipal.asetukset :refer [asetukset]]))

(defn in? [coll elem]
  (some #(= elem %) coll))

(defn parse-int [number-string]
  (try (Integer/parseInt number-string)
    (catch Exception e nil)))

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
        query-params (into {} (filter second params))
        next-url (format-url (str (-> @asetukset :server :base-url) api-url) (merge query-params {:since next-id}))]
    (if (some? data)
      (api-response {:data data
                     :pagination {:next_url (if next-id next-url nil)}})
      {:status 404})))

(defn add-index [key coll]
  (map #(assoc %1 key %2) coll (range)))