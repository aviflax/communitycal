(ns communitycal.llm
  (:require
   [communitycal.config :refer [config]])
  (:import
   (dev.langchain4j.model.anthropic AnthropicChatModel)
   (dev.langchain4j.model.openai OpenAiChatModel)))

(defn make-anthropic-model
  [model-name config-get]
  (-> (AnthropicChatModel/builder)
      (.apiKey (config-get :anthropic-key))
      (.modelName model-name)
      ; (.timeout (.ofSeconds 60))
      (.build)))

(defn make-openai-model
  [model-name config-get]
  (-> (OpenAiChatModel/builder)
      (.apiKey (config-get :openai-key))
      (.modelName model-name)
      ; (.timeout (.ofSeconds 60))
      (.build)))

(defn complete
  [prompt model]
  (.chat model prompt))

(comment
  (let [model (make-openai-model "gpt-4o-mini" config)]
    (complete "The cheese is old and moldy," model))

  ,)
