(ns extracter.core
  (:require [instaparse.core :as insta]
            [clojure.java.io :as io]))

(def parse (insta/parser (clojure.java.io/resource "doc.bnf")))

(def sec (insta/parser
          "S = Section
           Section = <#\"# \"> Heading <#\":\">
           Heading = #\"\w+\"
           "))
