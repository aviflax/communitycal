(ns communitycal.llm
  (:require
   [communitycal.config :refer [config]])
  (:import
   (dev.langchain4j.model.anthropic AnthropicChatModel)
   (dev.langchain4j.model.openai OpenAiChatModel)
   (java.time Duration)))

(def timeout-secs 90)
(def max-retries 2)

(defn make-anthropic-model
  [model-name config-get]
  (-> (AnthropicChatModel/builder)
      (.apiKey (config-get :anthropic-key))
      (.modelName model-name)
      (.timeout (Duration/ofSeconds timeout-secs))
      (.maxRetries (int max-retries))
      (.build)))

(defn make-openai-model
  [model-name config-get]
  (-> (OpenAiChatModel/builder)
      (.apiKey (config-get :openai-key))
      (.modelName model-name)
      (.timeout (Duration/ofSeconds timeout-secs))
      (.maxRetries (int max-retries))
      (.build)))

(defn complete
  [prompt model]
  (let [start (System/nanoTime)
        completion (.chat model prompt)
        duration (- (System/nanoTime) start)]
    {:completion completion
     :duration-ms (/ duration 1e6)}))

(comment
  (let [model (make-openai-model "gpt-4o-mini" config)]
    (complete "The cheese is old and moldy," model))

  (let [model (make-anthropic-model "claude-sonnet-4-20250514" config)]
    (complete "The cheese is old and moldy," model))

  ,)
