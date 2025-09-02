(ns communitycal.web.public.calendar
  (:require
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
