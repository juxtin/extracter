(ns extracter.core-test
  (:require [clojure.test :refer :all]
            [extracter.core :refer :all]))

(def examples
  {:fqdn [:Docs
          [:Doc
           [:Title "fqdn"]
           [:Section
            [:Heading "Purpose"]
            [:Body "Returns the fully qualified domain name of the host."]]
           [:Section
            [:Heading "Resolution"]
            [:Body "Simply joins the hostname fact with the domain name fact."]]
           [:Section
            [:Heading "Caveats"]
            [:Body
             "No attempt is made to check that the two facts are accurate or that"
             "the two facts go together. At no point is there any DNS resolution made"
             "either."]]]]
   :blockdevices [:Docs
                  [:Doc
                   [:Title "blockdevice_<devicename>_size"]
                   [:Section
                    [:Heading "Purpose"]
                    [:Body "Return the size of a block device in bytes"]]
                   [:Section
                    [:Heading "Resolution"]
                    [:Body
                     "Parse the contents of /sys/block/<device>/size to receive the size (multiplying by 512 to correct for blocks-to-bytes)"]]
                   [:Section
                    [:Heading "Caveats"]
                    [:Body
                     "Only supports Linux 2.6+ at this time, due to the reliance on sysfs"]]]
                  [:Doc
                   [:Title "blockdevice_<devicename>_vendor"]
                   [:Section
                    [:Heading "Purpose"]
                    [:Body
                     "Return the vendor name of block devices attached to the system"]]
                   [:Section
                    [:Heading "Resolution"]
                    [:Body
                     "Parse the contents of /sys/block/<device>/device/vendor to retrieve the vendor for a device"]]
                   [:Section
                    [:Heading "Caveats"]
                    [:Body
                     "Only supports Linux 2.6+ at this time, due to the reliance on sysfs"]]]
                  [:Doc
                   [:Title "blockdevice_<devicename>_model"]
                   [:Section
                    [:Heading "Purpose"]
                    [:Body
                     "Return the model name of block devices attached to the system"]]
                   [:Section
                    [:Heading "Resolution"]
                    [:Body
                     "Parse the contents of /sys/block/<device>/device/model to retrieve the model name/number for a device"]]
                   [:Section
                    [:Heading "Caveats"]
                    [:Body
                     "Only supports Linux 2.6+ at this time, due to the reliance on sysfs"]]]
                  [:Doc
                   [:Title "blockdevices"]
                   [:Section
                    [:Heading "Purpose"]
                    [:Body "Return a comma seperated list of block devices"]]
                   [:Section
                    [:Heading "Resolution"]
                    [:Body
                     "Retrieve the block devices that were identified and iterated over in the creation of the blockdevice_ facts"]]
                   [:Section
                    [:Heading "Caveats"]
                    [:Body
                     "Block devices must have been identified using sysfs information"]]]]})


(deftest parse-resource-test
  (testing "Parses the example fqdn fact documentation correctly."
    (is (= (parse-resource "test/fqdn.rb") (:fqdn examples))))
  (testing "Parses the example blockdevices fact documentation correctly."
    (let [expected [:Docs [:Doc [:Title "blockdevice_<devicename>_size"] [:Section [:Heading "Purpose"] [:Body "Return the size of a block device in bytes"]] [:Section [:Heading "Resolution"] [:Body "Parse the contents of /sys/block/<device>/size to receive the size (multiplying by 512 to correct for blocks-to-bytes)"]] [:Section [:Heading "Caveats"] [:Body "Only supports Linux 2.6+ at this time, due to the reliance on sysfs"]]] [:Doc [:Title "blockdevice_<devicename>_vendor"] [:Section [:Heading "Purpose"] [:Body "Return the vendor name of block devices attached to the system"]] [:Section [:Heading "Resolution"] [:Body "Parse the contents of /sys/block/<device>/device/vendor to retrieve the vendor for a device"]] [:Section [:Heading "Caveats"] [:Body "Only supports Linux 2.6+ at this time, due to the reliance on sysfs"]]] [:Doc [:Title "blockdevice_<devicename>_model"] [:Section [:Heading "Purpose"] [:Body "Return the model name of block devices attached to the system"]] [:Section [:Heading "Resolution"] [:Body "Parse the contents of /sys/block/<device>/device/model to retrieve the model name/number for a device"]] [:Section [:Heading "Caveats"] [:Body "Only supports Linux 2.6+ at this time, due to the reliance on sysfs" "# Fact: blockdevices"]]]]]
      (is (= expected (parse-resource "test/blockdevices.rb"))))))


