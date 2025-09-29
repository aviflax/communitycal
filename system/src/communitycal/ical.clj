(ns communitycal.ical
  (:require
   [clojure.string :as str]
   [communitycal.temporals :refer [date->zdt zdt->date]]
   [event :as-alias e]
   [icalendar :as-alias ical]
   [java-time.api :as jt])
  (:import
   (java.io StringReader)
   (net.fortuna.ical4j.data CalendarBuilder)
   (net.fortuna.ical4j.model Calendar Component Parameter Period Property)
   (net.fortuna.ical4j.model.component VEvent)
   (net.fortuna.ical4j.model.property Description ExDate Location RRule XProperty)))

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
  [{:event/keys [name start end timezone-id notes]
    ::ical/keys [rrule exdate]
    loc-name    :location/name}]
  (println "RRULE:" rrule "\nEXDATE:" exdate)
  (-> (VEvent.
        (date->zdt start timezone-id)
        (date->zdt end timezone-id)
        name)
      (.withProperty (Description. notes))
      (.withProperty (Location. loc-name))
      (.withProperty (RRule. rrule))
      ; Passing the value to the constructor does not seem to construct the property correctly.
      (.withProperty (doto (ExDate.) (.setValue exdate)))
      (.getFluentTarget)))

(defn vevent->event
  [^VEvent event]
  (merge #:event{:name        (-> event .getSummary .getValue)
                 :start       (-> event .getStartDate .get .getDate zdt->date)
                 :end         (-> event .getEndDate   .get .getDate zdt->date)
                 :timezone-id (-> event .getStartDate .get (.getParameter Parameter/TZID) .get .getValue)}
         (when-let [loc-name (-> event .getLocation .getValue)]
           {:location/name loc-name})
         (when-let [rrule (some-> event (.getProperty Property/RRULE) (.orElse nil) .getValue)]
           {::ical/rrule rrule})
         (when-let [exdate (some-> event (.getProperty Property/EXDATE) (.orElse nil) .getValue)]
           {::ical/exdate exdate})
         (when-let [notes (some-> event .getDescription .getValue)]
           {:event/notes notes})))

(defn parse-calendar
  [s]
  (-> (CalendarBuilder.)
      (.build (StringReader. s))))

(defn get-events
  [^Calendar calendar]
  (filter #(= (.getName %) Component/VEVENT)
          (.getComponents calendar)))

(defn get-occurrences
  [event]
  (let [now (jt/zoned-date-time) ;; TODO: use time zone id from event
        start (jt/minus now (jt/years 1))
        end (jt/plus now (jt/years 1))
        period (Period. start end)]
    (as-> event v
          (event->vevent v)
          (.calculateRecurrenceSet v period)
          (mapv (fn [period]
                  (merge event #:event{:start (-> period .getStart zdt->date)
                                       :end   (-> period .getEnd   zdt->date)}))
                v))))

(def ^:private not-blank? (complement str/blank?))

(defn recurring?
  [event]
  (boolean
    (cond
      (instance? VEvent event) (some-> event (.getProperty Property/RRULE) (.orElse nil) .getValue)
      (map? event) (some-> event ::ical/rrule not-blank?))))

(comment
  (make-calendar "Foo Bar")

  (event->vevent #:event{:name "Foo"
                         :notes "Bar"
                         :start (java.util.Date.)
                         :end (java.util.Date.)
                         :timezone-id "America/New_York"})
  ,)
