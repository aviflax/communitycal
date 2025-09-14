(ns communitycal.web.server
  (:require
   [clj-simple-router.core :as router]
   [communitycal.db :as db]
   [communitycal.web.calendar :as c]
   [communitycal.web.onboarding :as o]
   [communitycal.web.routing :as routing]
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
    (wrap-file "resources/web/static/")
    (wrap-content-type)
    (wrap-not-modified)))

(defn add-html
  [req]
  (update req :uri #(str % ".html")))

(defn handle-dynamic
  [req handler & args]
  (let [{:keys [response txs]} (apply handler (cons req args))]
    (when txs
      (try
        (d/transact (db/connect db/client) {:tx-data txs})
        (catch clojure.lang.ExceptionInfo e
          (throw (ex-info (.getMessage e) (assoc (ex-data e) :txs txs) e)))))
    (if (future? response)
      @response
      response)))

(def routes
  #_:clj-kondo/ignore  ; TODO: teach clj-kondo how to lint this macro
  (routing/resources
    "/editions"              [[:get req (-> req add-html handle-static)]]

    "/onboarding/start"      [[:get req (-> req add-html handle-static)]]
    "/onboarding/accounts"   [[:post req (handle-dynamic req o/post-accounts)]]
    "/onboarding/add-event"  [[:get  req (-> req add-html handle-static)]
                              [:post req (handle-dynamic req o/post-add-event)]]
    "/onboarding/add-event/frag/datalist/location-names"  [[:get req (handle-dynamic req o/get-frag-datalist-loc-names)]]
    "/onboarding/add-event/frag/datalist/event-names"     [[:get req (handle-dynamic req o/get-frag-datalist-event-names)]]
    "/onboarding/review"     [[:get req (handle-dynamic req o/get-review)]]
    "/onboarding/share"      [[:get req (-> req add-html handle-static)]]

    "/calendar/*/*"          [[:get
                               [comm-slug cal-slug :as req]
                               (handle-dynamic req c/get-cal comm-slug cal-slug)]]))

(def main-handler
  (-> handle-static
    (router/wrap-routes routes)
    wrap-params))

(defn -main [& {port :port, :or {port 3000}}]
  (db/init)
  (println "✅ DB initialized")
  (let [server (run-jetty main-handler {:port port :join? false})]
    (print "✅ HTTP server now listening on port" port)
    (flush)
    (.join server)))

(comment
  (db/init)

  (do
    (require '[ring.middleware.reload :refer [wrap-reload]])

    (def dev-handler
      (wrap-reload #'main-handler))

    (def dev-server (run-jetty dev-handler {:port 3000 :join? false})))

  (.stop dev-server)

  ,)
