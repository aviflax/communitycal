(ns communitycal.db.queries
  (:require
   [datomic.client.api :as d]))


(defn get-all-locations
  [db]
  (->> (d/q
         '[:find ?location
           :where
           [?e :event/location ?location]]
         db)
    (map first)))
