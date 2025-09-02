(ns communitycal.web.html
  (:require
   [hiccup.page :as hp]))

(defn ok-response
  [content]
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (str content)})

(defn page
  [{:keys [title header]} & main]
  (hp/html5
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
     [:title title]
     [:link {:rel "stylesheet" :href "https://cdn.simplecss.org/simple.min.css"}]
     [:link {:rel "stylesheet" :type "text/css" :href "../tweaks.css"}]
     [:script {:src "../js/vendored/htmx.2.0.6.min.js"}]]
    [:body
     [:header
      [:nav
       [:a {:href "../my/communities/riverdale-high-athletics"} "Riverdale High Athletics"]
       [:a {:href "../my/calendars/jv-bball-25-26"} "JV Basketball 25–26"]]
      [:h1 header]]
     (vec (cons :main main))]))
