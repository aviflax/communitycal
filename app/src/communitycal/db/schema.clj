(ns communitycal.db.schema
  (:refer-clojure :exclude [ref str]))


(defn- str
  ([ident]
   #:db{:ident       ident
        :valueType   :db.type/string
        :cardinality :db.cardinality/one})
  ([ident doc]
   (assoc (str ident) :doc doc)))


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


(def schemata
  {:init [(ref     :history/created-by)
          (instant :history/created-at)

          (id :community/id)
          (str :community/name)
   
          (id :calendar/id)
          (str :calendar/name)
   
          (id :user/id)
          (str :user/name)
          (-> (str :user/email)
              (assoc :db/unique :db.unique/identity))]})
