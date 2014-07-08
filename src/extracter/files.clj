(ns extracter.files
  (:refer-clojure :exclude [contains?])
  (:require [clojure.java.io :as io]))

(defn contains?
  [^java.util.regex.Pattern re ^String s]
  (boolean (re-find re s)))

(defn file?
  [^java.io.File f]
  (.isFile f))

(defn ruby-file?
  [^java.io.File f]
  (when (file? f)
    (let [name (.getName f)
          suffix (last (re-find #"\.(\w+)$" name))]
      (= suffix "rb"))))

(defn fact?
  "Returns true if the given file appears to contain at least one fact docstring."
  [^java.io.File f]
  (when (ruby-file? f)
    (with-open [fin (io/reader f)]
      (->> fin
           line-seq
           (some (partial contains? #"# Fact: "))))))

(defn facts-in-dir
  "Returns a lazy sequence of java.io.File instances for every fact source file in a directory path.
  Scans recursively."
  [^String path]
  (->> path
       io/file
       file-seq
       (filter fact?)))
