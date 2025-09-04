(ns communitycal.web.calendar
  (:require
   [communitycal.db :as db]
   [communitycal.db.queries :refer [get-all-events]]
   [communitycal.ical :refer [event->vevent make-calendar]]
   [communitycal.web.html :as html]))

(defn calendar-page
  []
  (html/page
    {:title "JV Basketball 25–26 (Riverdale High Athletics)"
     :header "Coming Soon"}
    [:h2 "For Real"]))

(defn get-calendar-page
  [_req]
  {:response (future (html/ok-response (calendar-page)))})

(defn get-calendar-ical
  [_req]
  {:response
   (future
     (let [db (db/get-db)
           events (get-all-events db)
           calendar (make-calendar "TODO")]
       (doseq [event events]
         (.add calendar (event->vevent event)))
       {:status 200
        :headers {"Content-Type" "text/calendar; charset=utf-8"}
        :body (str calendar)}))})
