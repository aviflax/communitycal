(ns communitycal.onboarding.queries
  (:require
   [datomic.client.api :as d]))

;; TODO: add community-id as an arg
(defn get-all-locations
  [db]
  (->>
    (d/q
      '[:find ?location
        :where [?e :event/location ?location]]
      db)
    (map first)))

;; TODO: add community-id as an arg
(defn get-all-event-names
  [db]
  (->>
    (d/q
      '[:find ?name
        :where [?e :event/name ?name]]
      db)
    (map first)))

;; TODO: add calendar-id as an arg
(defn get-all-events
  [db]
  (->>
    (d/q
      '[:find (pull ?e [*])
        :where [?e :event/name ?name]]
      db)
    (map first)
    (sort-by :event/start)))
