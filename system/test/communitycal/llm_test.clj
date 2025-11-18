(ns communitycal.llm-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [communitycal.config :as config :refer [config]]
   [communitycal.ical :as ical :refer [get-events parse-calendar validate vevent->event]]
   [communitycal.llm :as llm :refer [complete! make-anthropic-model make-openai-model]]
   [communitycal.string :refer [interpolate]])
  (:import
   (java.time Instant)
   (java.util Date)))

(defn- date
  [s]
  (Date/from (Instant/parse s)))

(deftest initial-event-prompt
  (config/validate!)
  (let [models {:gpt-5-nano                {:f make-openai-model     :max-duration-secs 6}
                :claude-sonnet-4-20250514  {:f make-anthropic-model  :max-duration-secs 6}}
        prompt-template-name "initial-event"
        prompt-template (slurp (str "resources/llm-prompt-templates/" prompt-template-name))
        tzid "America/New_York"
        prompt (interpolate
                 prompt-template
                 {:event-description "Practice in the school gym every Wednesday from 4:30pm to 6pm, from 9/17 to 11/12 except Oct 29"
                  :current-year "2025"
                  :timezone-id tzid})
        expected #:event{:name              "Practice"
                         :start             (date "2025-09-17T16:30:00-04:00")
                         :end               (date "2025-09-17T17:30:00-04:00")
                         :timezone-id       tzid
                         :location/name     "School gym"
                         :icalendar/rrule   "FREQ=WEEKLY;UNTIL=20251112T235959;BYDAY=WE"
                         :icalendar/exdates [#inst "2025-10-29T20:30:00.000-00:00"]}]
    (println prompt)
    ;; TODO: change this to do the I/O concurrently
    (doseq [[model-name {:keys [f max-duration-secs]}] models]
      (testing model-name
        (let [model (f model-name config)
              {:keys [completion duration-ms]} (complete! prompt model)
              _ (println (format "\n\n-----------\n%s\n-----------\n\n" completion))
              calendar (parse-calendar completion)
              vevent (-> calendar get-events first)
              event (vevent->event vevent)
              actual (dissoc event :icalendar/uid)
              prep (fn [m] (update-vals m #(if (string? %)
                                             (-> % str/lower-case (str/split #"[ ;]") first)
                                             %)))
              occurrences (ical/get-occurrences vevent)]
          (is (map? actual))
          (is (= (prep expected) (prep actual)))
          (is (empty? (validate calendar)))
          (is (str/includes? (or (some-> actual :location/name str/lower-case) "") "gym"))
          (is (= 3 (count occurrences)))
          (is (< duration-ms (* max-duration-secs 1000))))))))

(comment
  (require '[kaocha.repl :as k])

  (k/run)

  ,)
