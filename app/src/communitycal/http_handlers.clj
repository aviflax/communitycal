(ns communitycal.http-handlers
  (:require
   [clojure.pprint :refer [pprint]]))

(defn post-accounts
  [req]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body (with-out-str (pprint (:params req)))})
