(ns communitycal.db.schema
  (:refer-clojure :exclude [ref str]))


(defn- str
  "Construct a schema map for a string with a cardinality of one."
  ([ident] (str ident nil))
  ([ident doc]
   #:db{:ident       ident
        :valueType   :db.type/string
        :cardinality :db.cardinality/one
        :doc         doc}))


(defn- id
  "id"
  [ident]
  #:db{:ident       ident
       :valueType   :db.type/uuid
       :unique      :db.unique/identity
       :cardinality :db.cardinality/one})


(defn- instant
  "instant"
  [ident]
  #:db{:ident       ident
       :valueType   :db.type/instant
       :cardinality :db.cardinality/one})


(defn- ref
  "reference"
  [ident]
  #:db{:ident       ident
       :valueType   :db.type/ref
       :cardinality :db.cardinality/one})


(def schemata
  {:init [(ref     :history/created-by)
          (instant :history/created-at)

          (id :external/id)

          (str :community/name)
   
          (str :calendar/name)
   
          (str :user/name)
          (str :user/email)]})
