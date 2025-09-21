(ns communitycal.config
  (:require
   [environ.core :refer [env]]))

(defn config
  ([]
   {:openai-key (env :openai-api-key)
    :anthropic-key (env :anthropic-api-key)})
  ([k]
   (k (config))))

(comment
  (config :anthropic-key)

  ,)
