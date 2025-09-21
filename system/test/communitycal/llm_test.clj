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
          [model-name modelf] [["gpt-5-nano" make-openai-model]]]
    (testing (format "%s with %s" prompt-template-name model-name)
      (let [prompt-template (slurp (str "resources/llm-prompt-templates/" prompt-template-name))
            event-description "Practice in the school gym every Wednesday at 4:30 from 9/17 to 11/12 except Oct 29"
            tzid "America/New_York"
            prompt (format prompt-template event-description tzid)
            expected #:event{:name         "Practice"
                             :start        (date "2025-09-17T16:30:00-04:00")
                             :end          (date "2025-09-17T17:30:00-04:00")
                             :timezone-id  tzid
                             :notes        nil

                             :location/name "School gym"}
            model (modelf model-name config)
            completion (complete prompt model)
            _ (println (format "\n\n-----------\n%s\n-----------\n\n" completion))
            calendar (parse-calendar completion)
            event (-> calendar get-events first)
            actual (vevent->event event)
            prep (fn [m] (update-vals m #(if (string? %)
                                           (-> % str/lower-case (str/split #" ") first)
                                           %)))]
        (is (map? actual))
        (is (= (prep expected) (prep actual)) (format "completion text was: %s" completion))
        (is (str/includes? (or (some-> actual :location/name str/lower-case) "") "gym"))))))
