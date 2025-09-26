(ns communitycal.ical
  (:require
   [communitycal.temporals :refer [date->zdt zdt->date]]
   [java-time.api :as jt])
  (:import
   (java.io StringReader)
   (net.fortuna.ical4j.data CalendarBuilder)
   (net.fortuna.ical4j.model Calendar Component Parameter Period Property)
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
  [^VEvent event]
  (merge #:event{:name        (-> event .getSummary .getValue)
                 :start       (-> event .getStartDate .get .getDate zdt->date)
                 :end         (-> event .getEndDate   .get .getDate zdt->date)
                 :timezone-id (-> event .getStartDate .get (.getParameter Parameter/TZID) .get .getValue)}
         (when-let [loc-name (-> event .getLocation .getValue)]
           {:location/name loc-name})
         (when-let [rrule (some-> event (.getProperty Property/RRULE) (.orElse nil) .getValue)]
           {:event/recurrence rrule})
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

(defn get-ocurrences
  [event]
  (let [now (jt/local-date)
        start (jt/minus now (jt/years 1))
        end (jt/plus now (jt/years 1))
        vevent (if (instance? VEvent event) event (event->vevent event))]
    (.getOccurrences vevent (Period. start end))))

(defn recurring?
  [event]
  (boolean
    (cond
      (instance? VEvent event) (some-> event (.getProperty Property/RRULE) (.orElse nil) .getValue)
      (map? event) (:event/recurrence event))))

(comment
  (make-calendar "Foo Bar")

  (event->vevent #:event{:name "Foo"
                         :notes "Bar"
                         :start (java.util.Date.)
                         :end (java.util.Date.)
                         :timezone-id "America/New_York"})
  ,)
