(ns communitycal.db
  (:require
   [communitycal.db.schemata :as s]
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


(defn get-db
  []
  (d/db (connect client)))


(comment
  (init)
  (d/delete-database client config)

  (let [conn (connect client)
        db (d/db conn)]
    #_(d/pull
        db
        '[* {:person/community [*]}]
        [:person/email "g.grappler@riverdalehigh.edu"])

    (d/q
      '[:find [?location ...]
        :where
        [?e :event/location ?location]]
      db))
  )
