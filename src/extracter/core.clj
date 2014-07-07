(ns extracter.core
  (:gen-class)
  (:use [clojure.pprint])
  (:require [instaparse.core :as insta]
            [extracter.markdown :as md]
            [extracter.json :as json]
            [clojure.java.io :as io]
            [extracter.files :as files]
            [clojure.string :as s]
            [clojure.core.typed :refer [ann cf U Fn Any HVec Vec Keyword Seq HMap] :as t]
            [clojure.tools.cli :as cli]))

(ann ^:no-check clojure.java.io/resource [String -> java.io.File])
(ann ^:no-check clojure.java.io/reader [java.io.File -> java.io.BufferedReader])
(ann ^:no-check instaparase.core/parser [(U java.net.URL java.io.File java.lang.String) -> (Fn [String -> (Vec (U Vec Keyword String))])])
(ann ^:no-check instaparse.core/transform [HMap Seq -> Any])
(ann ^:no-check clojure.core/line-seq [java.io.BufferedReader -> (Vec String)])
(ann ^:no-check clojure.core/slurp [(U String java.io.File java.net.URL java.io.BufferedReader) -> String])
(ann ^:no-check parse (Fn [String -> (Vec (U Vec Keyword String))]))
(def parse (insta/parser (io/resource "doc.bnf")))

(ann code? [String -> Boolean])
(defn code?
  [s]
  (boolean (re-find #"^[^#]" s)))

(ann slurp-comments [java.io.File -> String])
(defn slurp-comments
  "Given an instance of java.io.File, return a string containing all of the comments until the code begins."
  [^java.io.File f]
  (->> f
       io/reader
       line-seq
       (take-while (complement code?))
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

(ann transform-file [java.io.File -> Any])
(defn transform-file
  [^java.io.File f]
  (t/tc-ignore (let [parse-tree (parse (slurp f))]
                 (insta/transform transformations parse-tree))))

(ann transform-facts-in-dir [String -> (Seq Any)])
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

(ann path->md [String String -> nil])
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
   ["-u" "--usage" "Print this help message"]]) ;; lein run -h shadows "help"

(ann run-markdown [String String -> nil])
(defn run-markdown
  [path output]
  (if (= output "docs.{md,json}")
    (run-markdown path "docs.md")
    (do (println "Scanning" path "for facts. Will output markdown to" output)
        (path->md path output))))

(ann run-json [String String -> nil])
(defn run-json
  [path output]
  (if (= output "docs.{md,json}")
    (run-json path "docs.json")
    (do (println "Scanning" path "for facts. Will output JSON to" output)
        (->> path
             transform-facts-in-dir
             json/pretty-encode
             (spit output)))))

(ann -main [String * -> nil])
(defn -main
  [& args]
  (let [opts (cli/parse-opts args cli-options)
        {:keys [usage in out markdown json]} (:options opts)]
    (cond
     usage (print (:summary opts))
     json (run-json in out)
     markdown (run-markdown in out))))
