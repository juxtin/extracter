(ns extracter.core
  (:gen-class)
  (:use [clojure.pprint])
  (:require [instaparse.core :as insta]
            [extracter.markdown :as md]
            [extracter.json :as json]
            [extracter.audit :as audit]
            [clojure.java.io :as io]
            [extracter.files :as files]
            [clojure.string :as s]
            [clojure.tools.cli :as cli]))

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

(def cli-options
  [["-o" "--out FILE" "The path to the output file"]
   ["-i" "--in PATH" "A path containing facts"]
   ["-m" "--markdown" "Output markdown"
    :default true
    :flag true]
   ["-j" "--json" "Output JSON"
    :default false
    :flag true]
   ["-u" "--usage" "Print this help message"]]) ;; lein run -h shadows "help"

(defn run-markdown
  [results output]
  (do (println "Outputting markdown to:" output)
      (md/facts->file results output)))

(defn run-json
  [results output]
  (println "Outputting JSON to:" output)
  (->> results
       json/pretty-encode
       (spit output)))

(defn -main
  [& args]
  (let [opts (cli/parse-opts args cli-options)
        {:keys [usage in out markdown json]} (:options opts)]
    (if (and in out (or markdown json)) ;; if the input is sufficient
      (do
        (println "Processing files in:" in)
        (let [results (transform-facts-in-dir in)]
          (cond
           json (run-json results out)
           markdown (run-markdown results out))
          (println "Completed run. Auditing...")
          (audit/find-missing in results)))
      (print (:summary opts))))) ;; if there's no clear directive, print usage info
