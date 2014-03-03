(ns user
  (:require [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.namespace.repl :as nsr]
            [clj-http.client :as hc]
            clojure.core.cache
            schema.core))

(schema.core/set-fn-validation! true)

(defonce ^:private palvelin (atom nil))

(defn ^:private kaynnista! []
  {:pre [(not @palvelin)]
   :post [@palvelin]}
  (require 'aipal.palvelin)
  (reset! palvelin ((ns-resolve 'aipal.palvelin 'kaynnista!)
                     (assoc @(ns-resolve 'aipal.asetukset 'oletusasetukset)
                            :development-mode true))))

(defn ^:private sammuta! []
  {:pre [@palvelin]
   :post [(not @palvelin)]}
  ((ns-resolve 'aipal.palvelin 'sammuta) @palvelin)
  (reset! palvelin nil))

(defn uudelleenkaynnista! []
  (when @palvelin
    (sammuta!))
  (nsr/refresh :after 'user/kaynnista!))
