(ns extracter.core
  (:use [clojure.pprint])
  (:require [instaparse.core :as insta]
            [clojure.java.io :as io]))

(def parse (insta/parser (io/resource "doc.bnf")))

(defn parse-resource
  [^String path]
  (let [rdr (io/reader (io/resource path))]
    (parse (slurp rdr))))

(defn retry
  []
  (with-redefs [parse (insta/parser (io/resource "doc.bnf"))]
    (pprint (parse-resource "blockdevices.rb"))))
