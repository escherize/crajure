(ns crajure.core-test
  (:require [clojure.test :refer :all]
            [crajure.core :refer :all]))

(def get-pages
  (fn []
    (query-cl {:query "fixie bike"
               :area "sfbay"
               :section :for-sale})))

(deftest cl-query-works
  (testing "all maps recieved from query-result are identical and listed."
    (let [query-result (get-pages)
          keys-in-result (->> query-result (map keys) set)]
      (is (= #{[:price :title :date :region :item-url :category]}
             keys-in-result)))))
