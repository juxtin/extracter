(ns extracter.markdown
  (:require [clojure.string :as s]))

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
  (->> body ;; this is structured lke this for historical reasons
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

(defn run
  [results output]
  (do (println "Outputting markdown to:" output)
      (facts->file results output)))
