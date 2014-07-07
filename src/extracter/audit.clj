(ns extracter.audit
  (:require [extracter.files :as files]
            [clojure.string :as s]
            [clojure.set :as set]
            [clojure.java.io :as io]))

(defn fact-names
  "Given a java.io.File object containing one or more documented facts, return the names of those facts."
  [^java.io.File f]
  (->> f
       slurp
       (re-seq #"Fact: ([\w_<>#]*)")
       (mapcat rest)))

(defn likely-facts-in-dir
  "Recursively scans a directory for documented facts and returns the names
   of any facts found. Relies on the '# Fact: x' convention."
  [^String path]
  (->> path
       files/facts-in-dir
       (mapcat fact-names)
       set))

(defn find-missing
  "Given a path and a results set, return a list of facts that appear to be documented but are not in the results."
  [^String path results]
  (let [actual (set (map :title results))
        expected (likely-facts-in-dir path)]
    (if-let [missing (set/difference expected actual)]
      (println "Missing from results:" (s/join ", " missing))
      (println "Audit passed. All documented facts are present in the output."))))