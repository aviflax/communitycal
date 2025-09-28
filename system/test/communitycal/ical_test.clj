(ns communitycal.ical-test
  (:require
   [clojure.data :as data :refer [diff]]
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [communitycal.ical :as nsut])
  (:import
   (net.fortuna.ical4j.model Calendar)))

(deftest parse-calendar
  (testing "Valid iCalendar Documents"
    (testing "Simple"
      (let [doc "BEGIN:VCALENDAR
                 VERSION:2.0
                 CALSCALE:GREGORIAN
                 PRODID:-//Your Organization//Your Product//EN
                 BEGIN:VEVENT
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

(deftest event->vevent
  (testing "basic case happy path"
    (let [event #:event{:name           "Practice"
                        :start          #inst "2025-09-17T20:30:00.000-00:00"
                        :end            #inst "2025-09-17T21:30:00.000-00:00"
                        :timezone-id    "America/New_York"
                        :recurrence     "FREQ=WEEKLY;UNTIL=20251112T235959;BYDAY=WE"
                        :location/name  "School gym"}
          vevent (nsut/event->vevent event)]
      (is (= (:event/name event)
             (some-> vevent .getSummary .getValue)))
      (is (= (:location/name event)
             (some-> vevent .getLocation .getValue))))))

(deftest vevent->event
  (testing "A VEvent that was triggering an exception"
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
                               :start          #inst "2025-09-17T20:30:00.000-00:00"
                               :end            #inst "2025-09-17T21:30:00.000-00:00"
                               :timezone-id    "America/New_York"
                               :recurrence     "FREQ=WEEKLY;UNTIL=20251112T235959;BYDAY=WE"
                               :location/name  "School gym"}
              event (first (nsut/get-events cal))
              actual (nsut/vevent->event event)]
      (is (= expected actual) (take 2 (diff expected actual))))))

(deftest get-ocurrences
  (let [event #:event{:name           "Practice"
                      :start          #inst "2025-09-17T20:30:00.000-00:00"
                      :end            #inst "2025-09-17T21:30:00.000-00:00"
                      :timezone-id    "America/New_York"
                      :recurrence     "FREQ=WEEKLY;UNTIL=20250925T235959;BYDAY=WE"
                      :location/name  "School gym"}
        expected [(dissoc event :event/recurrence)
                  (merge event #:event{:start #inst "2025-09-24T20:30:00.000-00:00"
                                       :end   #inst "2025-09-24T21:30:00.000-00:00"})]
        actual (->> (nsut/get-occurrences event)
                    (map nsut/vevent->event))]
    (is (= (count expected) (count actual)))
    (is (= expected actual) (take 2 (diff expected actual)))))
