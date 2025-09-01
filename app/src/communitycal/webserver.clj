(ns communitycal.webserver
  (:require
   [clj-simple-router.core :as router]
   [communitycal.db :as db]
   [communitycal.http-handlers.onboarding :as ho]
   [datomic.client.api :as d]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.file :refer [wrap-file]]
   [ring.middleware.not-modified :refer [wrap-not-modified]]
   [ring.middleware.params :refer [wrap-params]])
  (:gen-class))

(def not-found
  (constantly {:status 404 :headers {"Content-Type" "text/plain"} :body "Not Found"}))

(def handle-static
  (-> not-found
    (wrap-file "static")
    (wrap-content-type)
    (wrap-not-modified)))

(defn handle-dynamic
  [req handler]
  (let [{:keys [response txs]} (handler req)]
    (when txs
      (try
        (d/transact (db/connect db/client) {:tx-data txs})
        (catch clojure.lang.ExceptionInfo e
          (throw (ex-info (.getMessage e) (assoc (ex-data e) :txs txs) e)))))
    (if (future? response)
      @response
      response)))

(def routes
  (router/routes
    "GET  /onboarding/start"      req (handle-static (update req :uri #(str % ".html")))
    "POST /onboarding/accounts"   req (handle-dynamic req ho/post-accounts)
    "GET  /onboarding/add-event"  req (handle-static (update req :uri #(str % ".html")))
    "POST /onboarding/add-event"  req (handle-dynamic req ho/post-add-event)
    "GET  /onboarding/add-event/fragments/inputs/location" req (handle-dynamic req ho/get-fragments-inputs-location)
    "GET  /onboarding/review"     req (handle-dynamic req ho/get-review)))

(def main-handler
  (-> handle-static
    (router/wrap-routes routes)
    wrap-params))

(defn -main [& _args]
  (run-jetty main-handler {:port 3000}))

(comment
  (require '[ring.middleware.reload :refer [wrap-reload]])

  (db/init)

  (def dev-handler
    (wrap-reload #'main-handler))

  (def dev-server (run-jetty dev-handler {:port 3000 :join? false}))

  (.stop dev-server)
  ,)
