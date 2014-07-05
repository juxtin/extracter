(ns extracter.core
  (:gen-class)
  (:use [clojure.pprint])
  (:require [instaparse.core :as insta]
            [extracter.markdown :as md]
            [extracter.json :as json]
            [clojure.java.io :as io]
            [extracter.files :as files]
            [clojure.string :as s]
            [clojure.tools.cli :as cli]))

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

(def cli-options
  [["-o" "--out FILE" "The path to the output file"
    :default "docs.{md,json}"]
   ["-i" "--in PATH" "A path containing facts"
    :default "facter/lib/facter"]
   ["-m" "--markdown" "Output markdown"
    :default true
    :flag true]
   ["-j" "--json" "Output JSON"
    :default false
    :flag true]
   ["-h" "--help"]])

(defn run-markdown
  [path output]
  (if (= output "docs.{md,json}")
    (run-markdown path "docs.md")
    (do (println "Scanning" path "for facts. Will output markdown to" output)
        (path->md path output))))

(defn run-json
  [path output]
  (if (= output "docs.{md,json}")
    (run-json path "docs.json")
    (do (println "Scanning" path "for facts. Will output JSON to" output)
        (->> path
             transform-facts-in-dir
             json/pretty-encode
             (spit output)))))

(defn -main
  [& args]
  (let [opts (cli/parse-opts args cli-options)
        {:keys [help in out markdown json]} (:options opts)]
    (cond
     help (print (:summary opts))
     json (run-json in out)
     markdown (run-markdown in out))))
