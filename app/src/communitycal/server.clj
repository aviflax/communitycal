(ns communitycal.server
  (:gen-class)
  (:require
   [communitycal.http-handlers :as h]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.file :refer [wrap-file]]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.not-modified :refer [wrap-not-modified]]
   [ring.middleware.params :refer [wrap-params]]
   [clj-simple-router.core :as router]))


(def not-found
  (constantly {:status 404
               :headers {"Content-Type" "text/plain"}
               :body "Not Found"}))


(def static-handler
  "Initially for CSS, images, things like that. But also for static HTML pages such as the home page."
  (-> not-found
    (wrap-file "static")
    (wrap-content-type)
    (wrap-not-modified)))


(def routes
  (router/routes
    "POST /accounts" req (h/post-accounts req)))


(def main-handler
  (-> static-handler
      (router/wrap-routes routes)
      wrap-params))


(defn -main [& _args]
  (run-jetty main-handler {:port 3000}))



(comment
  (require '[ring.middleware.reload :refer [wrap-reload]])

  (def dev-handler
    (wrap-reload #'main-handler))

  (def dev-server (run-jetty dev-handler {:port 3000 :join? false}))

  (.stop dev-server)

  )
