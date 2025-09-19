(ns communitycal.llm
  (:require
   [communitycal.config :refer [config]])
  (:import
   (dev.langchain4j.model.anthropic AnthropicChatModel)
   (dev.langchain4j.model.openai OpenAiChatModel)
   (java.time Duration)))

(def timeout-secs 60)

(defn make-anthropic-model
  [model-name config-get]
  (-> (AnthropicChatModel/builder)
      (.apiKey (config-get :anthropic-key))
      (.modelName model-name)
      (.timeout (Duration/ofSeconds timeout-secs))
      (.build)))

(defn make-openai-model
  [model-name config-get]
  (-> (OpenAiChatModel/builder)
      (.apiKey (config-get :openai-key))
      (.modelName model-name)
      (.timeout (Duration/ofSeconds timeout-secs))
      (.build)))

(defn complete
  [prompt model]
  (.chat model prompt))

(comment
  (let [model (make-openai-model "gpt-4o-mini" config)]
    (complete "The cheese is old and moldy," model))

  ,)
