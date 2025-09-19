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
  (testing "get-started"
    (let [prompt-template (slurp "resources/llm-prompt-templates/get-started")
          event-description "Practice in the school gym every Wednesday at 4:30 from 9/17 to 11/12 except Oct 29"
          prompt (format prompt-template event-description)
          expected (assoc #:event{:name "Practice"
                                  :start (date "2025-09-17T16:30:00-04:00")
                                  :end (date "2025-09-17T17:30:00-04:00")
                                  :timezone-id "America/New_York"
                                  :notes nil}
                          :location/name
                          "School gym")
          model (make-openai-model "gpt-4o-mini" config)
          completion (complete prompt model)
          actual (-> completion
                     (parse-calendar)
                     (get-events)
                     (first)
                     (vevent->event))
          prep   (fn [m]
                   (update-vals m #(-> % str str/lower-case (str/split #" ") first)))]
      (is (= (prep expected) (prep actual)) (format "completion text was: %s" completion))
      (is (str/includes? (-> actual :location/name str/lower-case) "gym")))))
