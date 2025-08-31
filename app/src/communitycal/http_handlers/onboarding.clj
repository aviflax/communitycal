(ns communitycal.http-handlers.onboarding
  "Each handler accepts a request and returns a map with [:response :txs].

   If :txs is present and a non-nil seqable of maps, then the transactions will be commited before
   the response is sent to the client. IEe. the response map assumes the success of those
   transactions. If the transactions fail, then an error response will be returned to the client.

   If a handler needs to do I/O, the value of response should be a future that will return a
   response map."
  (:require
   [datomic.api :as d]))


(defn post-accounts
  [{{:strs [community-name calendar-name user-name user-email]} :params :as _req}]
  (let [temp-user-id "user"
        temp-community-id "community"
        now (java.util.Date.)]
    {:response {:status 303 :headers {"location" "add-event"}}
     :txs [{:db/id temp-user-id
            :user/name user-name
            :user/email user-email
            :user/id (d/squuid)
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
   [_req]
   {:response {:status 200 :body "TODO"}})
