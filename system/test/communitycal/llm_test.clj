(ns communitycal.llm-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [communitycal.config :refer [config]]
   [communitycal.ical :refer [event->vevent get-events parse-calendar]]
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
          expected (event->vevent #:event{:name "Practice"
                                          :start (date "2025-09-17T16:30:00-04:00")
                                          :end (date "2025-09-17T17:30:00-04:00")
                                          :timezone-id "America/New_York"
                                          :notes ""})
          model (make-openai-model "gpt-4o-mini" config)
          actual (-> (complete prompt model)
                     (parse-calendar)
                     (get-events)
                     (first))]
         (is (= expected actual)))))
