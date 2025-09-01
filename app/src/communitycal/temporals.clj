(ns communitycal.temporals
  (:refer-clojure :exclude [format])
  (:import
   (java.time LocalDateTime ZoneId)
   (java.time.format DateTimeFormatter)))

(defn strs->date
  "Accepts a date string and a time string as produced by the corresponding browser form controls,
   and an IANA time zone ID string. Returns a java.util.DateTime."
  [date time zone-id]
  (let [datetime (str date "T" time ":00")
        local-datetime (LocalDateTime/parse datetime)
        zoned-datetime (.atZone local-datetime (ZoneId/of zone-id))
        instant (.toInstant zoned-datetime)]
    (java.util.Date/from instant)))

(defn date->local-date
  [^java.util.Date date zone-id]
  (-> date
    .toInstant
    (.atZone (ZoneId/of zone-id))
    .toLocalDate))

(def formatters
  {:review-group (DateTimeFormatter/ofPattern "EEEE, d MMM ’yy")})

(defn format
  [temporal formatter-name]
  (.format temporal (get formatters formatter-name)))
