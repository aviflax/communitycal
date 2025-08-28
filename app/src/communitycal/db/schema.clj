(ns communitycal.db.schema)


(defn s
  "Construct a schema map for a string with a cardinality of one."
  ([ident] (s ident nil))
  ([ident doc]
   #:db{:ident       ident
        :valueType   :db.type/string
        :cardinality :db.cardinality/one
        :doc         doc}))


(def schema
  [(s :community/name)
   (s :calendar/name)
   (s :user/name)
   (s :user/email)])
