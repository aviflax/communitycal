(ns communitycal.web.calendar
  (:require
   [calendar :as-alias cal]
   [clojure.string :as str]
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
(defn- get-page
  [_req comm-slug cal-slug]
  {:response (future (html/ok-response (calendar-page)))})

;; TODO: validate slugs/relationships
(defn- get-ical
  [_req comm-slug cal-slug]
  {:response
   (future
     (let [db (db/get-db)
           events (get-all-events db)
           {cal-nom ::cal/name} (d/pull db [::cal/name] [::cal/slug cal-slug])]
       (if-not cal-nom
         {:status 404 :headers {"Content-Type" "text/plain"} :body "Not Found"}
         (let [calendar (make-calendar cal-nom)]
           (doseq [event events]
             (.add calendar (event->vevent event)))
           {:status 200
            :headers {"Content-Type" "text/calendar; charset=utf-8"}
            :body (str calendar)}))))})

(defn get
  [req comm-slug cal-slug]
  ;; TODO: actual content negotiation
  (if (str/ends-with? cal-slug ".ics")
    (get-ical req comm-slug (str/replace cal-slug ".ics" ""))
    (get-page req comm-slug cal-slug)))
