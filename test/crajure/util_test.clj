(ns crajure.util-test
  (:require [crajure.util :refer :all]
            [clojure.test :refer :all]))

(deftest util-test
  (testing "->flat-seq"
    (is (= [1] (->flat-seq 1) (->flat-seq [1])))))
