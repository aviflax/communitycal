(ns communitycal.webserver
  (:gen-class)
  (:require
   [clj-simple-router.core :as router]
   [communitycal.db :as db]
   [communitycal.http-handlers :as h]
   [datomic.client.api :as d]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.file :refer [wrap-file]]
   [ring.middleware.not-modified :refer [wrap-not-modified]]
   [ring.middleware.params :refer [wrap-params]]))


(def not-found
  (constantly {:status 404 :headers {"Content-Type" "text/plain"} :body "Not Found"}))


(def static-handler
  "Initially for CSS, images, things like that. But also for static HTML pages such as the home page."
  (-> not-found
    (wrap-file "static")
    (wrap-content-type)
    (wrap-not-modified)))


(defn handle-with-db
  [req handler]
  (let [{:keys [response txs]} (handler req)]
    (when txs
      (try
        (d/transact (db/connect db/client) {:tx-data txs})
        (catch clojure.lang.ExceptionInfo e
          (throw (ex-info (.getMessage e) (assoc (ex-data e) :txs txs) e)))))
    response))


(def routes
  (router/routes
    "GET  /onboarding/start"      req (static-handler (update req :uri #(str % ".html")))
    "POST /onboarding/accounts"   req (handle-with-db req h/post-accounts)
    "GET  /onboarding/add-event"  req (static-handler (update req :uri #(str % ".html")))))


(def main-handler
  (-> static-handler
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

  )
