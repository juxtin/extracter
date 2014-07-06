(ns extracter.json
  (:require [cheshire.core :as json]
            [clojure.core.typed :refer [ann cf U Fn Any Vec Keyword Kw Seq HMap] :as t]))

(ann ^:no-check cheshire.core/encode [Any (HMap :optional {:pretty Boolean}) -> String])
(ann pretty-encode [Any -> String])
(defn pretty-encode
  "Returns a pretty-printed JSON representation of the given data."
  [x]
  (json/encode x {:pretty true}))
