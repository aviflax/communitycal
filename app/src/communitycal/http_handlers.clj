(ns communitycal.http-handlers
  "Each handler accepts a request and returns a map with [:response :txs].

   If :txs is present and a non-nil seqable of maps, then the transactions will be commited before
   the response is sent to the client. IEe. the response map assumes the success of those
   transactions. If the transactions fail, then an error response will be returned to the client.

   If a handler needs to do I/O, the value of response should be a future that will return a
   response map."
  (:require
   [clojure.pprint :refer [pprint]]
   [datomic.api :as d]))


(defn post-accounts
  [{{:strs [community-name calendar-name user-name user-email]} :params :as req}]
  (let [temp-user-id "foo"
        now (java.util.Date.)
        txs [{:db/id temp-user-id
              :user/name user-name
              :user/email user-email
              :user/id (d/squuid)
              :history/created-at now}

             {:community/name community-name
              :community/id (d/squuid)
              :history/created-by temp-user-id
              :history/created-at now}

             {:calendar/name calendar-name
              :calendar/id (d/squuid)
              :history/created-by temp-user-id
              :history/created-at now}]]
    {:response {:status 200
                :headers {"Content-Type" "text/plain"}
                :body (with-out-str
                        (pprint txs)
                        (println "\n\n")
                        (pprint req))}
     :txs txs}))
