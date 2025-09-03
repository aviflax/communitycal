(ns communitycal.web.routing
  (:require
   [clojure.string :as str]))

(def ^:private http-methods
  [:get :head :post :put :delete :connect :options :trace :patch])

;; TODO this hard-codes 405 responses for HEAD and OPTIONS, but that’s not right at all.
(defmacro resources
  {:based-on 'clj-simple-router.core/routes}
  [& body]
  (let [req-sym 'req
        routes (apply hash-map body)
        handled (into {}
                  (for [[path methods] routes
                        [method params handler] methods]
                    [(str (str/upper-case (name method)) " " path)
                     `(fn [~req-sym]
                        (let [~params ~(if (vector? params)
                                         `(:path-params ~req-sym)
                                         req-sym)]
                          ~handler))]))
        unhandled (into {}
                    (for [[path methods] routes
                          method http-methods
                          :let [handled (set (map first methods))]
                          :when (not (handled method))]
                      [(str (str/upper-case (name method)) " " path)
                       `(constantly {:status 405
                                     :headers {"Content-Type" "text/plain"}
                                     :body "Method Not Allowed"})]))]
    (merge handled unhandled)))

(comment
  (resources
    "/foo"  [[:get [] {:status 204}]
             [:post [] {:status 204}]]
    "/bar"  [[:get [] {:status 204}]])
  ,)
