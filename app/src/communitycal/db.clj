(ns communitycal.db
  (:require
   [communitycal.db.schema :as s]
   [datomic.client.api :as d]))


(def config {:server-type :datomic-local
             :system "communitycal"
             :db-name "main"})


(def client (d/client config))


(defn connect
  [client]
  (d/connect client config))


(defn init
  []
  (d/create-database client config)
  (d/transact (connect client) {:tx-data (:init s/schemata)}))



(comment
  (init)
  (d/delete-database client config)

  (let [conn (connect client)
        db (d/db conn)]
    (d/pull
      db
      '[* {:user/community [*]}]
      [:user/email "g.grappler@riverdalehigh.edu"]))
  )
