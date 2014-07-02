(ns extracter.markdown
  (:require [clojure.string :as s]))

(defn section->md
  [{:keys [body heading]}]
  (vec (concat [(format "**%s**:" heading) ""] [body ""])))

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
