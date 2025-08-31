(ns communitycal.db.schemata
  (:refer-clojure :exclude [ref str]))


(defn- str
  ([ident]
   #:db{:ident       ident
        :valueType   :db.type/string
        :cardinality :db.cardinality/one})
  ([ident doc]
   (assoc (str ident) :db/doc doc)))


(defn- bool
  [ident]
  #:db{:ident       ident
       :valueType   :db.type/boolean
       :cardinality :db.cardinality/one})


(defn- id
  [ident]
  #:db{:ident       ident
       :valueType   :db.type/uuid
       :cardinality :db.cardinality/one
       :unique      :db.unique/identity})


(defn- instant
  [ident]
  #:db{:ident       ident
       :valueType   :db.type/instant
       :cardinality :db.cardinality/one})


(defn- ref
  [ident]
  #:db{:ident       ident
       :valueType   :db.type/ref
       :cardinality :db.cardinality/one})


(defn- unique
  [attr]
  (assoc attr :db/unique :db.unique/identity))


(def schemata
  {:init [(ref     :history/created-by)
          (instant :history/created-at)

          (id :community/id)
          (str :community/name)
   
          (id :calendar/id)
          (ref :calendar/community)
          (str :calendar/name)
   
          (id :person/id)
          (str :person/name)
          (-> (str :person/email) unique)

          (id :event/id)
          (ref :event/calendar)
          (str :event/name)
          (str :event/location)
          (str :event/timezone-id "IANA region-based time zone ID such as Asia/Tel_Aviv")
          (instant :event/start)
          (instant :event/end)
          (bool :event/all-day)
          (bool :event/recurring)
          (str :event/notes)]})
