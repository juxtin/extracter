(ns extracter.core
  (:use [clojure.pprint])
  (:require [instaparse.core :as insta]
            [clojure.java.io :as io]
            [clojure.string :as s]))

(def parse (insta/parser (io/resource "doc.bnf")))

(defn parse-resource
  [^String path]
  (let [rdr (io/reader (io/resource path))]
    (parse (slurp rdr))))

(def transformations
  {:BodyContinued str
   :BodyMain str
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

(comment
(defn res [path]
  (slurp (io/reader (io/resource path))))

(defn retry []
  (with-redefs [parse (insta/parser (io/reader (io/resource "doc.bnf")))]
    (pprint (parse-resource "blockdevices.rb"))))

  )
