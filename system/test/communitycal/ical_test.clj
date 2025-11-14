(ns communitycal.ical-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [communitycal.ical :as nsut :refer [get-prop-val get-prop-vals]]
   [event :as-alias e]
   [icalendar :as-alias ical]
   [java-time.api :as jt])
  (:import
   (net.fortuna.ical4j.model Calendar Property)
   (net.fortuna.ical4j.model.property DateListProperty)))

(deftest parse-calendar-test
  (testing "Valid iCalendar Documents"
    (testing "Simple"
      (let [doc "BEGIN:VCALENDAR
                 VERSION:2.0
                 CALSCALE:GREGORIAN
                 PRODID:-//Your Organization//Your Product//EN
                 BEGIN:VEVENT
                 UID:20250917T163000-1@example.com
                 SUMMARY:Practice
                 DTSTART;TZID=America/New_York:20231101T163000
                 DTEND;TZID=America/New_York:20231101T173000
                 RRULE:FREQ=WEEKLY;BYDAY=WE;UNTIL=20231112T235959Z
                 EXDATE:20231029T163000
                 END:VEVENT
                 END:VCALENDAR"
            doc' (str/join "\n" (str/split doc #"\n +"))
            actual (nsut/parse-calendar doc')]
        (is (= Calendar (type actual)))))))

(deftest event->vevent-test
  (testing "basic case happy path"
    (let [event #:event{:name           "Practice"
                        :start          #inst "2025-09-17T20:30:00.000-00:00"
                        :end            #inst "2025-11-17T21:30:00.000-00:00"
                        :timezone-id    "America/New_York"
                        ::ical/uid      (str (random-uuid))
                        ::ical/rrule    "FREQ=WEEKLY;COUNT=5;BYDAY=WE"
                        ::ical/exdates  [#inst "2025-09-24T20:30:00.000-00:00"
                                         #inst "2025-10-01T20:30:00.000-00:00"]
                        :location/name  "School gym"}
          vevent (nsut/event->vevent event)]
      ;; TODO: add at least one assertion for each property (or at least most)
      (is (= (:event/name event) (some-> vevent .getSummary .getValue)))
      (is (= (:location/name event) (some-> vevent .getLocation .getValue)))
      (is (= (::ical/uid event) (get-prop-val vevent Property/UID)))
      (is (= (::ical/rrule event) (get-prop-val vevent Property/RRULE)))
      (is (= (::ical/exdates event)
             (->> (get-prop-vals vevent Property/EXDATE DateListProperty/.getDates)
                  (map jt/java-date))))
      (let [validation-results (-> vevent .validate .getEntries)]
        (is (empty? validation-results))))))

(deftest vevent->event-test
  (testing "basic case happy path"
    (let [doc "BEGIN:VCALENDAR
               VERSION:2.0
               PRODID:-//Example//EN
               BEGIN:VEVENT
               UID:20250917T163000-1@example.com
               DTSTAMP:20250921T120000Z
               SUMMARY:Practice
               LOCATION:School gym
               DTSTART;TZID=America/New_York:20250917T163000
               DTEND;TZID=America/New_York:20250917T173000
               RRULE:FREQ=WEEKLY;BYDAY=WE;UNTIL=20251112T235959
               EXDATE:20251029T163000
               END:VEVENT
               END:VCALENDAR"
              doc' (str/join "\n" (str/split doc #"\n +"))
              cal (nsut/parse-calendar doc')
              expected #:event{:name           "Practice"
                               :start          #inst "2025-09-17T20:30:00"
                               :end            #inst "2025-09-17T21:30:00"
                               :timezone-id    "America/New_York"
                               ::ical/uid      "20250917T163000-1@example.com"
                               ::ical/rrule    "FREQ=WEEKLY;UNTIL=20251112T235959;BYDAY=WE"
                               ::ical/exdates  [#inst "2025-10-29T20:30:00"]
                               :location/name  "School gym"}
              event (first (nsut/get-events cal))
              actual (nsut/vevent->event event)]
      (is (= expected actual)))))

(deftest get-ocurrences-test
  (testing "basic case, no exceptions"
    (let [event #:event{:name           "Practice"
                        :start          #inst "2025-09-17T20:30:00.000-00:00"
                        :end            #inst "2025-09-17T21:30:00.000-00:00"
                        :timezone-id    "America/New_York"
                        ::ical/uid      (str (random-uuid))
                        ::ical/rrule    "FREQ=WEEKLY;COUNT=3;BYDAY=WE"
                        ::ical/exdates  []
                        :location/name  "School gym"}
          expected [event
                    (merge event #:event{:start #inst "2025-09-24T20:30:00.000-00:00"
                                         :end   #inst "2025-09-24T21:30:00.000-00:00"})
                    (merge event #:event{:start #inst "2025-10-01T20:30:00.000-00:00"
                                         :end   #inst "2025-10-01T21:30:00.000-00:00"})]
          actual (nsut/get-occurrences event)]
      (is (= (count expected) (count actual)))
      (is (= expected actual) (str "ACTUAL START DATES:" (mapv ::e/start actual)))))
  (testing "one exception"
    (let [event #:event{:name           "Practice"
                        :start          #inst "2025-09-17T20:30:00.000-00:00"
                        :end            #inst "2025-09-17T21:30:00.000-00:00"
                        :timezone-id    "America/New_York"
                        ::ical/uid      (str (random-uuid))
                        ::ical/rrule    "FREQ=WEEKLY;COUNT=3;BYDAY=WE"
                        ::ical/exdates  [#inst "2025-09-24T20:30:00.000-00:00"]
                        :location/name  "School gym"}
          expected [event
                    ;; omitting the middle Wednesday on Sep 24 as per the exdate
                    (merge event #:event{:start #inst "2025-10-01T20:30:00.000-00:00"
                                         :end   #inst "2025-10-01T21:30:00.000-00:00"})]
          actual (nsut/get-occurrences event)]
      (is (= (count expected) (count actual)))
      (is (= expected actual) (str "ACTUAL START DATES:" (mapv ::e/start actual))))))

(comment
  (require '[kaocha.repl :as k])

  (k/run)

  ,)
