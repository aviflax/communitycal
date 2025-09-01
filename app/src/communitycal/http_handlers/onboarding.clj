(ns communitycal.http-handlers.onboarding
  "Each handler accepts a request and returns a map with [:response :txs].

   If :txs is present and a non-nil seqable of maps, then the transactions will be commited before
   the response is sent to the client. IEe. the response map assumes the success of those
   transactions. If the transactions fail, then an error response will be returned to the client.

   If a handler needs to do I/O, the value of response should be a future that will return a
   response map."
  (:require
   [communitycal.db :as db]
   [communitycal.db.queries :as q]
   [datomic.api :as d]
   [hiccup.page :as hp]
   [hiccup2.core :as h])
  (:import
   (java.time LocalDateTime ZoneId)))

(defn- strs->date
  "Accepts a date string and a time string as produced by the corresponding browser form controls,
   and an IANA time zone ID string. Returns a java.util.DateTime."
  [date time zone-id]
  (let [datetime (str date "T" time ":00")
        local-datetime (LocalDateTime/parse datetime)
        zoned-datetime (.atZone local-datetime (ZoneId/of zone-id))
        instant (.toInstant zoned-datetime)]
    (java.util.Date/from instant)))

(defn post-accounts
  [{{:strs [community-name calendar-name user-name user-email]} :params :as _req}]
  (let [temp-user-id "user"
        temp-community-id "community"
        now (java.util.Date.)]
    {:response {:status 303 :headers {"location" "add-event"}}
     :txs [{:db/id temp-user-id
            :person/name user-name
            :person/email user-email
            :person/id (d/squuid)
            :provenance/created-at now}

           {:db/id temp-community-id
            :community/name community-name
            :community/id (d/squuid)
            :provenance/created-by temp-user-id
            :provenance/created-at now}

           {:calendar/name calendar-name
            :calendar/community temp-community-id
            :calendar/id (d/squuid)
            :provenance/created-by temp-user-id
            :provenance/created-at now}]}))

(defn post-add-event
  [{{:strs [event-name location timezone-id start-date start-time all-day end-date end-time
            recurring notes next-page]}
    :params
    :as _req}]
  (let [start (strs->date start-date start-time timezone-id)
        end (strs->date end-date end-time timezone-id)
        txs [{:event/name event-name
              :event/location location
              :event/timezone-id timezone-id
              :event/start start
              :event/end end
              :event/notes notes
              :event/all-day (boolean all-day)
              :event/recurring (boolean recurring)}]]
    {:response {:status 303 :headers {"location" next-page}}
     :txs txs}))

(defn- html-ok-response
  [content]
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (str content)})

(defn get-fragments-inputs-location
  [_req]
  {:response
   (future
     (let [db (db/get-db)
           locations (q/get-all-locations db)
           frag [:input {:type :text
                         :id :location
                         :name :location
                         :list :locations
                         :required true
                         :minlength 3
                         :placeholder (first locations)}
                 [:datalist {:id :locations}
                  (for [loc locations]
                    [:option {:value loc}])]]]
       (html-ok-response (h/html frag))))})

(defn- page
  [{:keys [title header main]}]
  (hp/html5
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
     [:title title]
     [:link {:rel "stylesheet" :href "https://cdn.simplecss.org/simple.min.css"}]
     [:link {:rel "stylesheet" :type "text/css" :href "../tweaks.css"}]
     [:script {:src "../js/vendored/htmx.2.0.6.min.js"}]]
    [:body
     [:header
      [:nav
       [:a {:href "../my/communities/riverdale-high-athletics"}
        "Riverdale High Athletics"]
       [:a {:href "../my/calendars/jv-bball-25-26"}
        "JV Basketball 25–26"]]
      [:h1 header]]
     [:main main]]))

(defn- date->local-date
  [^java.util.Date date zone-id]
  (-> date
      .toInstant
      (.atZone (ZoneId/of zone-id))
      .toLocalDate))

(defn- review-page
  []
  (let [events-by-date (->> (db/get-db)
                            (q/get-all-events)
                            (group-by #(date->local-date (:event/start %) (:event/timezone-id %))))]
    (page
      {:title "Review Events « JV Basketball 25–26 « Riverdale High Athletics « CommunityCal Free"
       :header "Review Events"
       :main (for [[date events] events-by-date]
               [:section.day
                [:h2 date]
                (for [{:event/keys [name location start end notes]} events]
                  [:details.event
                   [:summary
                    [:div.event-headline
                     [:div.event-time (str start "–" end)]
                     [:div.event-name name]
                     [:div.event-loc location]
                     "✏️ 🗑️"]]
                   notes])])})))

(defn get-review
  [_req]
  {:response (future (html-ok-response (review-page)))})
