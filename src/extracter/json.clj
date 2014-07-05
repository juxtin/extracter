(ns extracter.json
  (:require [cheshire.core :as json]))

(defn pretty-encode
  [x]
  (json/encode x {:pretty true}))
