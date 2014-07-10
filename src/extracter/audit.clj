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

(defn fail-audit
  [actual expected]
  (let [missing (set/difference expected actual)
        extra (set/difference actual expected)]
    (println "Audit failed!")
    (when (empty? actual)
      (println "No facts were documented!"))
    (when (seq missing)
      (println "These facts appear to be documented, but are missing from the results:" (s/join ", " missing)))
    (when (seq extra)
      (println "These facts appear in the results, but I couldn't find their documentation:" (s/join ", " extra))))
  false)

(defn audit
  "Given a path and a results set, run the following audit checks: 
     1. Check that the output contains at least one fact.
     2. Look for facts that appear to have documentation, but don't appear in the output. 
     3. Look for facts that do appear in the output, but have no documentation in the source.
  All three checks will run, even if the first one or two fail. Returns true if all three pass,
  false otherwise."
  [^String path results]
  (let [actual (set (map :title results))
        expected (likely-facts-in-dir path)]
    (if
     (= actual expected) (do (println "Audit passed. All documented facts are present in the output and there are no unexpected facts.") true)
     (fail-audit actual expected))))
