(ns extracter.json
  (:require [cheshire.core :as json]))

(defn pretty-encode
  [x]
  (json/encode x {:pretty true}))

(defn run
  [results output]
  (println "Outputting JSON to:" output)
  (->> results
       pretty-encode
       (spit output)))
