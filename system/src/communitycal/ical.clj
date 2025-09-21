(ns communitycal.ical
  (:require
   [communitycal.temporals :refer [date->zdt temporal->date]])
  (:import
   (java.io StringReader)
   (net.fortuna.ical4j.data CalendarBuilder)
   (net.fortuna.ical4j.model Calendar Component)
   (net.fortuna.ical4j.model.component VEvent)
   (net.fortuna.ical4j.model.property Description XProperty)))

(def company-name "Calendrical")
(def product-name "CommunityCal")
(def language "EN")
(def product-id (format "-//%s//%s//%s" company-name product-name language))

(defn make-calendar
  [nom]
  (-> (Calendar.)
      (.withProdId product-id)
      (.withDefaults)
      (.withProperty (XProperty. "X-WR-CALNAME" nom))
      (.getFluentTarget)))

(defn event->vevent
  [{:event/keys [name start end timezone-id notes]}]
  (-> (VEvent.
        (date->zdt start timezone-id)
        (date->zdt end timezone-id)
        name)
      (.withProperty (Description. notes))
      (.getFluentTarget)))

(defn vevent->event
  [^VEvent event
   ^String tz-id]
  #:event{:name         (some-> event .getSummary .getValue)
          :start        (some-> (.getStartDate event) (.orElse nil) (.getDate) (temporal->date tz-id))
          :end          (some-> (.getEndDate event)   (.orElse nil) (.getDate) (temporal->date tz-id))
          :timezone-id  tz-id
          :notes        (some-> event .getDescription .getValue)

          :location/name (some-> event .getLocation .getValue)})

(defn parse-calendar
  [s]
  (-> (CalendarBuilder.)
      (.build (StringReader. s))))

(defn get-events
  [^Calendar calendar]
  (filter #(= (.getName %) Component/VEVENT)
          (.getComponents calendar)))

(defn get-tzid
  ^String
  [^Calendar calendar]
  (->> calendar
       .getComponents
       (filter #(= (.getName %) Component/VTIMEZONE))
       first
       .getTimeZoneId
       .getValue))

(comment
  (make-calendar "Foo Bar")

  (event->vevent #:event{:name "Foo"
                         :notes "Bar"
                         :start (java.util.Date.)
                         :end (java.util.Date.)
                         :timezone-id "America/New_York"})
  ,)
