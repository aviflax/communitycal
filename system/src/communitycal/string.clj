(ns communitycal.string
  (:require
   [clojure.string :as str]))

(defn interpolate
  [template substitutions]
  (reduce-kv
    (fn [s k v] (str/replace s (str "{" (name k) "}") (str v)))
    template
    substitutions))
