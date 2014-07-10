(ns extracter.audit
  (:require [extracter.files :as files]
            [clojure.string :as s]
            [clojure.set :as set]
            [clojure.java.io :as io]))

(defn fact-names
  "Given a java.io.File object containing one or more (possibly malformed) documented facts, return the
  names of those facts."
  [^java.io.File f]
  (->> f
       slurp
       (re-seq #"[Ff]act:[ ]+(.*)\n")
       (map second)))

(defn likely-facts-in-dir
  "Recursively scans a directory for documented facts and returns a set of the names of any facts found. 
  Relies on the 'Fact: x' convention, but applied much more loosely than the main parsing function."
  [^String path]
  (->> path
       io/file
       file-seq
       (filter files/ruby-file?)
       (mapcat fact-names)
       set))

(defn find-missing
  "Given a path and a results set, print a list of facts that appear to be documented but are not in the results.
  Returns true if there are any missing facts, false otherwise."
  [^String path results]
  (let [actual (set (map :title results))
        expected (likely-facts-in-dir path)
        missing (set/difference expected actual)]
    (if (empty? missing)
      (do (println "Audit passed. All documented facts are present in the output.") false)
      (do (println "Missing from results:" (s/join ", " missing)) true))))
