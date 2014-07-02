(ns extracter.files
  (:require [clojure.java.io :as io]))

(defn contains?
  [^java.util.regex.Pattern re ^String s]
  (boolean (re-find re s)))

(defn fact?
  "Returns true if the given file appears to contain at least one fact docstring."
  [^java.io.File f]
  (with-open [fin (io/reader f)]
    (->> fin
         line-seq
         (some (partial contains? #"# Fact: ")))))

(defn file?
  [^java.io.File f]
  (.isFile f))

(defn facts-in-dir
  "Returns a lazy sequence of java.io.File instances for every fact source file in a directory path.
  Scans recursively."
  [^String path]
  (->> path
       io/file
       file-seq
       (filter file?)
       (filter fact?)))
