(ns communitycal.ical-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [communitycal.ical :as nsut])
  (:import
   (net.fortuna.ical4j.data ParserException)))

(deftest parse-calendar
  (testing "Invalid iCalendar Documents"
    (testing "invalid: VTIMEZONE is missing BEGIN and END lines and the TZID property"
      (let [doc "BEGIN:VCALENDAR
                 VERSION:2.0
                 CALSCALE:GREGORIAN
                 PRODID:-//Your Organization//Your Product//EN
                 VTIMEZONE:America/New_York
                 BEGIN:STANDARD
                 TZOFFSETFROM:-0400
                 TZOFFSETTO:-0500
                 TZNAME:EST
                 DTSTART:20221106T020000
                 END:STANDARD
                 BEGIN:DAYLIGHT
                 TZOFFSETFROM:-0500
                 TZOFFSETTO:-0400
                 TZNAME:EDT
                 DTSTART:20220313T020000
                 END:DAYLIGHT
                 END:VTIMEZONE
                 BEGIN:VEVENT
                 SUMMARY:Practice
                 DTSTART:20231101T163000
                 DTEND:20231101T173000
                 RRULE:FREQ=WEEKLY;BYDAY=WE;UNTIL=20231112T235959Z
                 EXDATE:20231029T163000
                 END:VEVENT
                 END:VCALENDAR"
            doc' (str/join "\n" (str/split doc #"\n +"))]
        (is (thrown-with-msg?
              ParserException
              #".+component\.Standard cannot be cast to class.+"
              (nsut/parse-calendar doc')))))))

(deftest vevent->event
  (testing "A VEvent that was triggering an exception"
    (let [doc "BEGIN:VCALENDAR
              VERSION:2.0
              PRODID:-//Example//EN
              BEGIN:VTIMEZONE
              TZID:America/New_York
              X-LIC-LOCATION:America/New_York
              BEGIN:DAYLIGHT
              TZOFFSETFROM:-0500
              TZOFFSETTO:-0400
              TZNAME:EDT
              DTSTART:19700308T020000
              RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=2SU
              END:DAYLIGHT
              BEGIN:STANDARD
              TZOFFSETFROM:-0400
              TZOFFSETTO:-0500
              TZNAME:EST
              DTSTART:19701101T020000
              RRULE:FREQ=YEARLY;BYMONTH=11;BYDAY=1SU
              END:STANDARD
              END:VTIMEZONE
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
              expected #:event{:name "Practice"
                               :start #inst "2025-09-17T20:30:00.000-00:00"
                               :end #inst "2025-09-17T21:30:00.000-00:00"
                               :timezone-id "America/New_York"
                               :notes nil
                               :location/name "School gym"}
              event (first (nsut/get-events cal))
              tzid (nsut/get-tzid cal)
              actual (nsut/vevent->event event tzid)]
      (is (= expected actual)))))
