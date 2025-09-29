(ns communitycal.config
  (:require
   [clojure.string :as str]
   [environ.core :refer [env]]))

(def ^:private required [:anthropic-api-key :openai-api-key])

(defn validate!
  []
  (when-let [missing (seq (filter (comp str/blank? env) required))]
    (throw (ex-info "Required config values are missing or blank" {:missing missing}))))

(defn config
  ([]
   {:openai-key (env :openai-api-key)
    :anthropic-key (env :anthropic-api-key)})
  ([k]
   (k (config))))

(comment
  (config :anthropic-key)
  (validate!)
  ,)
