(ns extracter.markdown
  (:require [clojure.string :as s]
            [clojure.core.typed :refer [ann cf U Fn Any HVec Value Vec Keyword Seq HMap Map] :as t]))

(ann ^:no-check clojure.string/escape [String (HMap (Value Any) String) -> String])
(ann html-escape [String -> String])
(defn html-escape
  [md]
  (s/escape md {\> "&gt;"
                \< "&lt;"
                \& "&amp;"}))

(ann bullet [(Seq String) -> (Vec String)])
(defn bullet
  "Given a collection of strings (assumed to be the body of a section),
  turn them into a markdown bulleted list if that list would have more
  than one item. Otherwise, return the body unchanged."
  [body]
  (if (>= (count body) 2)
    (mapv (partial str "* ") body)
    body))

(defn body-preprocess
  [body]
  (->> body
       (map html-escape)
       bullet))

(defn section->md
  [{:keys [body heading]}]
  (when (not (empty? body))
    [(format "**%s**:" heading) "" (body-preprocess body) ""]))

(defn title->md
  [title]
  (format "## `%s`" title))

(defn fact->md
  [{:keys [title sections]}]
  (vec
   (concat
    [(title->md title)]
    ["" ""] ;two blank lines
    (mapcat section->md sections)
    ["" "([↑ Back to top](#page-nav))"])))

(defn facts->file
  [facts ^String path]
  (->> facts
       (map fact->md)
       (interpose ["" "* * *" ""])
       flatten
       (s/join "\r\n")
       (spit path)))
