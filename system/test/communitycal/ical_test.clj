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
