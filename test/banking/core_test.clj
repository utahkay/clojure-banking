(ns banking.core-test
  (:require [clojure.test :refer :all]
            [banking.core :refer :all]))

(def my-checking-account (atom 0))

(deftest test-balance
  (is (= 0 (balance my-checking-account))))
