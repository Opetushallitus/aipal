(ns arvo.rest-api.admin
  (:require [buddy.auth.middleware :refer (wrap-authentication)]
            [compojure.api.sweet :refer :all]
            [buddy.auth.backends.token :refer (jws-backend)]
            [schema.core :as s]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [oph.common.util.http-util :refer [response-or-404]]
            [arvo.service.osio-tunnukset :as o]))


(defroutes admin-routes
  (POST "/osio-tunnukset" []
    :body [osio-data s/Any]
    (let [res (o/luo-osio-tunnukset (:ohjaus-kyselykerta osio-data) (:osio-kyselykerrat osio-data))]
      (response-or-404 res))))
