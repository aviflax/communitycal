(ns communitycal.slugs
  (:require
   [clojure.string :as str]
   [sluj.core :refer [sluj]]))

(def replacements
  {#"(?i)basketball" "bball"})

(defn slugify
  [s]
  (sluj (reduce (fn [accum [pattern with]]
                  (str/replace accum pattern with))
                s
                replacements)))

(comment
  ;; That’s an en dash in there in the input, a dash/hyphen in the output
  (= (slugify "JV Basketball 24—25")
     "jv-bball-24-25")

  ,)
