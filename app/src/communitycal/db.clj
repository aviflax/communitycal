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


  (d/pull (d/db (connect client)) '[*] [:user/email "foo@bar"])
  )
