(ns communitycal.web.onboarding
  "Each handler accepts a request and returns a map with [:response :txs].

   If :txs is present and a non-nil seqable of maps, then the transactions will be commited before
   the response is sent to the client. IEe. the response map assumes the success of those
   transactions. If the transactions fail, then an error response will be returned to the client.

   If a handler needs to do I/O, the value of response should be a future that will return a
   response map."
  (:require
   [communitycal.db :as db]
   [communitycal.db.queries :as q]
   [communitycal.slugs :refer [slugify]]
   [communitycal.temporals :as t]
   [communitycal.web.html :as html]
   [datomic.api :as d]
   [hiccup2.core :as h]))

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
            :community/slug (slugify community-name)
            :community/id (d/squuid)
            :provenance/created-by temp-user-id
            :provenance/created-at now}

           {:calendar/name calendar-name
            :calendar/slug (slugify calendar-name)
            :calendar/community temp-community-id
            :calendar/id (d/squuid)
            :provenance/created-by temp-user-id
            :provenance/created-at now}]}))

(defn post-add-event
  [{{:strs [event-name location-name timezone-id start-date start-time all-day end-date end-time
            recurring notes next-page]}
    :params
    :as _req}]
  (let [start (t/strs->date start-date start-time timezone-id)
        end (t/strs->date end-date end-time timezone-id)
        now (java.util.Date.)
        location-tmp-id "location"
        txs [;; TODO: add :location/community
             {:db/id location-tmp-id
              :location/name location-name
              :location/id (d/squuid)
              ;; TODO: add :provenance/created-by
              :provenance/created-at now}

             ;; TODO: add :event/calendar
             {:event/id (d/squuid)
              :event/name event-name
              :event/location location-tmp-id
              :event/timezone-id timezone-id
              :event/start start
              :event/end end
              :event/notes notes
              :event/all-day (boolean all-day)
              :event/recurring (boolean recurring)
              ;; TODO: add :provenance/created-by
              :provenance/created-at now}]]
    {:response {:status 303 :headers {"location" next-page}}
     :txs txs}))

(defn get-fragments-inputs-event-name
  [_req]
  {:response
   (future
     (let [db (db/get-db)
           event-names (q/get-all-event-names db)
           frag [:input {:type :text
                         :id :event-name
                         :name :event-name
                         :list :event-names
                         :required true
                         :minlength 3
                         :placeholder (first event-names)}
                 [:datalist {:id :event-names}
                  (for [event-name event-names]
                    [:option {:value event-name}])]]]
       (html/ok-response (h/html frag))))})

(defn get-frag-datalist-loc-names
  [_req]
  {:response
   (future
     (let [db (db/get-db)
           locations (q/get-all-location-names db)
           frag [:datalist
                 {:id :location-names}
                 (for [loc locations]
                   [:option {:value loc}])]]
       (html/ok-response (h/html frag))))})

(defn- review-page
  []
  (let [events-by-date (->> (db/get-db)
                            (q/get-all-events)
                            (group-by #(t/date->local-date (:event/start %) (:event/timezone-id %))))]
    (html/page
      {:title "Review Events « JV Basketball 25–26 « Riverdale High Athletics « CommunityCal Free"
       :header "Review Events"}
      (for [[date events] events-by-date]
        [:section.day
         [:h2 (t/format date :review-group)]
         (for [{:event/keys [name location start end timezone-id notes]} events
               :let [zstart (t/date->zdt start timezone-id)
                     zend   (t/date->zdt end timezone-id)]]
           [:details.event
            [:summary
             [:div.event-headline
              [:div.event-time (str (t/format zstart :time) "–" (t/format zend :time))]
              [:div.event-name name]
              [:div.event-loc location]
              "✏️ 🗑️"]]
            notes])])
      [:section
       [:h2 "How’s it look?"]
       [:a {:class "button", :href "add-event"}
        [:b "+"] " Let’s add another event"]
       [:br]
       [:br]
       [:a {:class "button", :href "share"}
        "🚀 Looks good; let’s share it!"]])))

(defn get-review
  [_req]
  {:response (future (html/ok-response (review-page)))})
