(ns communitycal.llm-test
  (:require
   [clojure.data :as data :refer [diff]]
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [communitycal.config :refer [config]]
   [communitycal.ical :refer [get-events parse-calendar vevent->event]]
   [communitycal.llm :refer [complete! make-anthropic-model make-openai-model]]
   [communitycal.string :refer [interpolate]])
  (:import
   (java.time Instant)
   (java.util Date)))

(defn- date
  [s]
  (Date/from (Instant/parse s)))

(deftest initial-event-prompt
  (let [models {:gpt-5-nano                {:f make-openai-model     :max-duration-secs 60}
                :claude-sonnet-4-20250514  {:f make-anthropic-model  :max-duration-secs 6}}
        prompt-template-name "initial-event"
        prompt-template (slurp (str "resources/llm-prompt-templates/" prompt-template-name))
        tzid "America/New_York"
        prompt (interpolate
                 prompt-template
                 {:event-description "Practice in the school gym every Wednesday at 4:30 from 9/17 to 11/12 except Oct 29"
                  :current-year "2025"
                  :timezone-id tzid})
        expected #:event{:name              "Practice"
                         :start             (date "2025-09-17T16:30:00-04:00")
                         :end               (date "2025-09-17T17:30:00-04:00")
                         :timezone-id       tzid
                         :notes             nil
                         :location/name     "School gym"
                         :icalendar/rrule   "FREQ=WEEKLY;BYDAY=WE;UNTIL=20251112T235959"
                         :icalendar/exdate  #inst "2025-10-29T20:30:00.000-00:00"}]
    ;; TODO: change this to do the I/O concurrently
    (doseq [[model-name {:keys [f max-duration-secs]}] models]
      (testing model-name
        (let [model (f model-name config)
              {:keys [completion duration-ms]} (complete! prompt model)
              _ (println (format "\n\n-----------\n%s\n-----------\n\n" completion))
              calendar (parse-calendar completion)
              event (-> calendar get-events first)
              actual (vevent->event event)
              prep (fn [m] (update-vals m #(if (string? %)
                                             (-> % str/lower-case (str/split #" ") first)
                                             %)))]
          (is (map? actual))
          (is (= (prep expected) (prep actual)) (take 2 (diff expected actual)))
          (is (str/includes? (or (some-> actual :location/name str/lower-case) "") "gym"))
          (is (< duration-ms (* max-duration-secs 1000))))))))
