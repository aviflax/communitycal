(ns communitycal.temporals
  (:refer-clojure :exclude [format])
  (:import
   (java.time Instant LocalDateTime ZoneId)
   (java.time.format DateTimeFormatter)
   (java.time.temporal Temporal)
   (java.util Date)))

(defn strs->date
  "Accepts a date string and a time string as produced by the corresponding browser form controls,
   and an IANA time zone ID string. Returns a java.util.DateTime."
  [date time zone-id]
  (let [datetime (str date "T" time ":00")
        local-datetime (LocalDateTime/parse datetime)
        zoned-datetime (.atZone local-datetime (ZoneId/of zone-id))
        instant (.toInstant zoned-datetime)]
    (java.util.Date/from instant)))

(defn inst->zdt
  [^Instant inst zone-id]
  (.atZone inst (ZoneId/of zone-id)))

(defn date->zdt
  [^Date date zone-id]
  (-> date
      .toInstant
      (inst->zdt zone-id)))

(defn date->local-date
  [^Date date zone-id]
  (-> date
      .toInstant
      (inst->zdt zone-id)
      .toLocalDate))

(defn zdt->date
  [zdt]
  (java.util.Date/from (.toInstant zdt)))

(def formatters
  {:review-group          (DateTimeFormatter/ofPattern "EEEE, d MMM ’yy")
   :day-of-week-full      (DateTimeFormatter/ofPattern "EEEE")
   :day-of-week-short     (DateTimeFormatter/ofPattern "EEE")
   :day-month-year-short  (DateTimeFormatter/ofPattern "d MMM ’yy")
   :time                  (DateTimeFormatter/ofPattern "HH:MM")})

(defn format
  [^Temporal temporal formatter-name]
  (.format temporal (get formatters formatter-name)))
