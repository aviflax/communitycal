(ns communitycal.web.calendar
  (:require
   [calendar :as-alias cal]
   [communitycal.db :as db]
   [communitycal.db.queries :refer [get-all-events]]
   [communitycal.ical :refer [event->vevent make-calendar]]
   [communitycal.web.html :as html]
   [datomic.client.api :as d]))

;; TODO: render an actual page
(defn calendar-page
  []
  (html/page
    {:title "JV Basketball 25–26 (Riverdale High Athletics)"
     :header "Coming Soon"}
    [:h2 "For Real"]))

;; TODO: validate slugs/relationships, retrieve calendar, render calendar, etc
(defn get-calendar-page
  [_req community-slug calendar-slug]
  {:response (future (html/ok-response (calendar-page)))})

;; TODO: validate slugs/relationships
(defn get-calendar-ical
  [_req community-slug calendar-slug]
  {:response
   (future
     (let [db (db/get-db)
           events (get-all-events db)
           {cal-nom ::cal/name} (d/pull db [::cal/name] [::cal/slug calendar-slug])]
       (if-not cal-nom
         {:status 404 :headers {"Content-Type" "text/plain"} :body "Not Found"}
         (let [calendar (make-calendar cal-nom)]
           (doseq [event events]
             (.add calendar (event->vevent event)))
           {:status 200
            :headers {"Content-Type" "text/calendar; charset=utf-8"}
            :body (str calendar)}))))})
