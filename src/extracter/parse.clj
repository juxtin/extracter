(ns extracter.parse
  (:require [instaparse.core :as insta]
            [extracter.files :as files]
            [clojure.java.io :as io]
            [clojure.string :as s]))

(def doc-parser (insta/parser (io/resource "doc.bnf")))

(defn parse
  "Parse x (file or string) using the CFG for comment docs. If the result is nil, log that."
  [x]
  (if-let [result (doc-parser x)]
    result
    (println "Error while parsing:" x)))

(defn code?
  [s]
  (re-find #"^[^#]" s))

(defn slurp-comments
  "Given an instance of java.io.File, return a string containing all of the comments until the code begins."
  [^java.io.File f]
  (->> f
       io/reader
       line-seq
       (take-while (complement code?))
       (map s/trimr)
       (s/join "\n")))

(def transformations
  {:BodyContinued str
   :BodyMain str
   :BodySection (fn [& sections] (s/join " " sections))
   :Body (fn body [& lines] {:body (vec lines)})
   :Heading (fn heading [title] {:heading title})
   :Section merge
   :Title str
   :Doc (fn [title & maps] {:title title :sections (vec maps)})
   :Docs (fn [& facts] {:facts (vec facts)})})

(defn transform-file
  "Given an instance of java.io.File, parse and transform it. Returns a vector of hash-maps."
  [^java.io.File f]
  {:pre [(files/fact? f)]}
  (let [parse-tree (parse (slurp-comments f))]
    (insta/transform transformations parse-tree)))

(defn transform-facts-in-dir
  "Recursively scans the path for files containing documented facts.
   Parses and transforms the fact docs and returns them as a vector of maps."
  [^String path]
  (->> path
       files/facts-in-dir
       (map slurp-comments)
       (pmap parse)
       (map (partial insta/transform transformations))
       flatten
       (filter :title))) ;; we don't want "facts" with nil values
