(ns crajure.core-test
  (:require [clojure.test :refer :all]
            [crajure.core :refer :all]))

(deftest cl-query-works
  (testing "cl-query returns the right stuff"
    (let [query-result (query-cl {:query "thing fixie bikes"
                                  :area "sfbay"
                                  :section :for-sale})
          keys-in-result (-> query-result :items first keys)]
      (is (= [:price :title :date :region :item-url :category]
             keys-in-result)))))
