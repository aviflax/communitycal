(ns build
  (:require
   [clojure.tools.build.api :as b]))

(def lib 'communitycal-server)
;; (def git-short-sha (b/git-process {:git-args "rev-parse --short HEAD"}))
;; (def version git-short-sha)
(def class-dir "target/classes")
(def uber-file (format "target/%s-standalone.jar" (name lib)))

;; delay to defer side effects (artifact downloads)
(def basis (delay (b/create-basis {:project "deps.edn"})))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis @basis
                  :ns-compile '[communitycal.web.server]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis @basis
           :main 'communitycal.web.server}))
