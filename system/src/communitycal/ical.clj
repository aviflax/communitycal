(ns communitycal.ical
  (:require
   [clojure.string :as str]
   [communitycal.temporals :refer [date->zdt temporal->date]]
   [event :as-alias e]
   [icalendar :as-alias ical]
   [java-time.api :as jt])
  (:import
   (java.io StringReader)
   (net.fortuna.ical4j.data CalendarBuilder)
   (net.fortuna.ical4j.model Calendar Component DateList Parameter Period Property)
   (net.fortuna.ical4j.model.component VEvent)
   (net.fortuna.ical4j.model.property DateListProperty Description ExDate Location RRule Uid XProperty)))

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
    ::ical/keys [rrule exdates uid]
    loc-name    :location/name}]
  (let [builder (-> (VEvent.
                      (date->zdt start timezone-id)
                      (date->zdt end timezone-id)
                      name)
                    (.withProperty (Uid. uid))
                    (.withProperty (Description. notes))
                    (.withProperty (Location. loc-name))
                    (.withProperty (RRule. rrule)))]
    (.withProperty builder (ExDate. (DateList. (map #(date->zdt % timezone-id) exdates))))
    (.getFluentTarget builder)))

(defn get-prop-val
  [vevent prop]
  (some-> vevent (.getProperty prop) (.orElse nil) (.getValue)))

(defn get-prop-vals
  ([vevent prop]
   (get-prop-vals vevent prop Property/.getValue))
  ([vevent prop getter]
   (->> (.getProperties vevent (into-array String [prop]))
        (mapcat getter)
        (doall)
        (seq))))

(defn vevent->event
  [^VEvent event]
  (let [tzid  (-> event .getStartDate .get (.getParameter Parameter/TZID) .get .getValue)
        desc  (some-> event .getDescription .getValue)]
    (merge #:event{:name        (-> event .getSummary .getValue)
                   :start       (-> event .getStartDate .get .getDate temporal->date)
                   :end         (-> event .getEndDate   .get .getDate temporal->date)
                   :timezone-id tzid
                   ::ical/uid   (or (get-prop-val event Property/UID) ;; TODO: if UID is missing/blank that’s invalid; should we throw instead?
                                    (str (random-uuid)))}
           (when-not (str/blank? desc)
             {:event/notes desc})
           (when-let [loc-name (-> event .getLocation .getValue)]
             {:location/name loc-name})
           (when-let [rrule (get-prop-val event Property/RRULE)]
             {::ical/rrule rrule})
           (when-let [exdates (get-prop-vals event Property/EXDATE DateListProperty/.getDates)]
             {::ical/exdates (map #(temporal->date % tzid) exdates)}))))

(defn validate
  "Returns a collection of errors."
  [component]
  (-> component .validate .getEntries))

(defn valid?
  [component]
  (-> component validate seq boolean not))

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
                  (merge event #:event{:start (-> period .getStart temporal->date)
                                       :end   (-> period .getEnd   temporal->date)}))
                v))))

(def ^:private not-blank? (complement str/blank?))

(defn recurring?
  [event]
  (boolean
    (cond
      (instance? VEvent event)  (get-prop-val event Property/RRULE)
      (map? event)              (some-> event ::ical/rrule not-blank?))))

(comment
  (make-calendar "Foo Bar")

  (event->vevent #:event{:name "Foo"
                         :notes "Bar"
                         :start (java.util.Date.)
                         :end (java.util.Date.)
                         :timezone-id "America/New_York"})
  ,)
