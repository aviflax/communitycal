(ns communitycal.web.routing
  {:clj-kondo/config '{:linters {:unresolved-symbol {:level :off}}}}
  (:require
   [clj-simple-router.core :as router]
   [clojure.string :as str]))

(defmacro resources
  {:based-on clj-simple-router.core/routes}
  [& body]
  (let [req-sym 'req]
    (into {}
      (for [[path routes] (partition 2 body)
            [method params handler] routes]
        [(str (str/upper-case (name method)) " " path)
         `(fn [~req-sym]
            (let [~params ~(if (vector? params)
                             `(:path-params ~req-sym)
                             req-sym)]
              ~handler))]))))
