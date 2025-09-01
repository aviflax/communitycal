(ns communitycal.db.queries
  (:require
   [datomic.client.api :as d]))

(defn get-all-locations
  [db]
  (->>
    (d/q
      '[:find ?location
        :where
        [?e :event/location ?location]]
      db)
    (map first)))

(defn get-all-events
  [db]
  (->>
    (d/q
      '[:find (pull ?e [*])
        :where [?e :event/name ?name]]
      db)
    (map first)))
