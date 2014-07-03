(ns extracter.core
  (:use [clojure.pprint])
  (:require [instaparse.core :as insta]
            [extracter.markdown :as md]
            [clojure.java.io :as io]
            [extracter.files :as files]
            [clojure.string :as s]
            [cheshire.core :as json]))

(def parse (insta/parser (io/resource "doc.bnf")))

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
       (s/join "\n")))

(defn parse-resource
  [^String path]
  (let [rdr (io/reader (io/resource path))]
    (parse (slurp rdr))))

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

(defn transform-resource
  [^String path]
  (let [parse-tree (parse-resource path)]
    (insta/transform transformations parse-tree)))

(defn transform-file
  [^java.io.File f]
  (let [parse-tree (parse (slurp f))]
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
       flatten))

(defn path->md
  [^String path ^String fout-path]
  (let [facts (transform-facts-in-dir path)]
    (md/facts->file facts fout-path)))

(comment
(defn res [path]
  (slurp (io/reader (io/resource path))))

(defn retry []
  (with-redefs [parse (insta/parser (io/reader (io/resource "doc.bnf")))]
    (pprint (parse-resource "blockdevices.rb"))))

  )
