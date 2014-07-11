(ns extracter.core
  (:gen-class)
  (:use [clojure.pprint])
  (:require [extracter.parse :as parse]
            [extracter.markdown :as md]
            [extracter.json :as json]
            [extracter.audit :as audit]
            [clojure.tools.cli :as cli]))

(def cli-options
  [["-o" "--out FILE" "The path to the output file"]
   ["-i" "--in PATH" "A path containing facts"]
   ["-m" "--markdown" "Output markdown"
    :default false
    :flag true]
   ["-j" "--json" "Output JSON"
    :default false
    :flag true]
   ["-v" "--version" "Print the version number"]]) ;; lein run -h shadows "help"

(defn run
  "Parse the input file and process the results with the right helper.
  After that, run the audit and exit 0 if the audit passes."
  [in out markdown json]
  (println "Processing files in:" in)
  (let [results (parse/transform-facts-in-dir in)]
    (cond
     json (json/run results out)
     markdown (md/run results out))
    (println "Completed run. Auditing...")
    (if (audit/audit in results)
      (System/exit 0)
      (System/exit 1))))

(defn -main
  [& args]
  (let [opts (cli/parse-opts args cli-options)
        {:keys [in out markdown json version]} (:options opts)]
    (cond
     (and in out (or markdown json)) (run in out markdown json)
     version (println "Extracter version 0.1.0")
     :else (print (:summary opts))))) ;; if there's no clear directive, print usage info
