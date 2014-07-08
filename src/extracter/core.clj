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
    :default true
    :flag true]
   ["-j" "--json" "Output JSON"
    :default false
    :flag true]
   ["-u" "--usage" "Print this help message"]]) ;; lein run -h shadows "help"

(defn run
  "Parse the input file and process the results with the right helper.
  After that, begin the audit."
  [in out markdown json]
  (println "Processing files in:" in)
  (let [results (parse/transform-facts-in-dir in)]
    (cond
     json (json/run results out)
     markdown (md/run results out))
    (println "Completed run. Auditing...")
    (audit/find-missing in results)))

(defn -main
  [& args]
  (let [opts (cli/parse-opts args cli-options)
        {:keys [usage in out markdown json]} (:options opts)]
    (if (and in out (or markdown json)) ;; if the input is sufficient
      (run in out markdown json)
      (print (:summary opts))))) ;; if there's no clear directive, print usage info
