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
   [hiccup2.core :as h])
  (:import
   [java.time LocalDateTime ZoneId]))


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
            :history/created-at now}

           {:db/id temp-community-id
            :community/name community-name
            :community/id (d/squuid)
            :history/created-by temp-user-id
            :history/created-at now}

           {:calendar/name calendar-name
            :calendar/community temp-community-id
            :calendar/id (d/squuid)
            :history/created-by temp-user-id
            :history/created-at now}]}))


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
       {:status 200
        :headers {"content-type" "text/html"}
        :body (str (h/html frag))}))})
