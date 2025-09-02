(ns communitycal.web.routing
  (:require
   [clojure.string :as str]))

(defmacro resources
  {:based-on 'clj-simple-router.core/routes}
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
