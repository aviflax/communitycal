(ns communitycal.config
  (:require
   [environ.core :refer [env]]))

(defn config
  ([]
   {:openai-key (env :openai-api-key)})
  ([k]
   (k (config))))
