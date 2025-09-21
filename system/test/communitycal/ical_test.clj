(ns communitycal.ical-test
  (:require
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
                               :notes          nil
                               :location/name  "School gym"}
              event (first (nsut/get-events cal))
              actual (nsut/vevent->event event)]
      (is (= expected actual)))))
