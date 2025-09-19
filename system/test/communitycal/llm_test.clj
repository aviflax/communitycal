(ns communitycal.llm-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [communitycal.config :refer [config]]
   [communitycal.ical :refer [get-events parse-calendar vevent->event]]
   [communitycal.llm :refer [complete make-openai-model]])
  (:import
   (java.time Instant)
   (java.util Date)))

(defn- date
  [s]
  (Date/from (Instant/parse s)))

(deftest prompts
  (doseq [prompt-template-name ["get-started"]
          [model-name modelf] [["gpt-4o-mini" make-openai-model]]]
    (testing (format "%s with %s" prompt-template-name model-name)
      (let [prompt-template (slurp (str "resources/llm-prompt-templates/" prompt-template-name))
            event-description "Practice in the school gym every Wednesday at 4:30 from 9/17 to 11/12 except Oct 29"
            time-zone-id "America/New_York"
            prompt (format prompt-template event-description time-zone-id)
            expected (assoc #:event{:name "Practice"
                                    :start (date "2025-09-17T16:30:00-04:00")
                                    :end (date "2025-09-17T17:30:00-04:00")
                                    :timezone-id time-zone-id
                                    :notes nil}
                            :location/name
                            "School gym")
            model (modelf model-name config)
            completion (complete prompt model)
            actual (-> completion
                       (parse-calendar)
                       (get-events)
                       (first)
                       (vevent->event))
            prep (fn [m] (update-vals m #(if (string? %)
                                           (-> % str/lower-case (str/split #" ") first)
                                           %)))]
        (is (= (prep expected) (prep actual)) (format "completion text was: %s" completion))
        (is (str/includes? (or (some-> actual :location/name str/lower-case) "") "gym"))))))
