(ns banking.core-test
  (:require [clojure.test :refer :all]
            [banking.core :refer :all]))

(def checking (make-account))
(def savings (make-account))

(defn my-fixture [f]
  (dosync
  (ref-set checking 100)
  (ref-set savings 100))
  (f))

(use-fixtures :each my-fixture)

(deftest test-credit
  (credit checking 25)
  (is (= 125 (balance checking))))

(deftest test-debit
  (debit checking 100)
  (is (= 0 (balance checking))))

(deftest test-overdraw-exception
  (is (thrown? Exception
        (debit checking 101))))

(deftest test-concurrent-credits-debits
  (doall (pmap #(do
                  (credit checking (+ % 1))
                  (debit checking %))
               (take 100 (repeat 5))))
  (is (= 200 (balance checking))))

(deftest test-transfer
  (transfer savings checking 25)
  (is (= 75 (balance savings)))
  (is (= 125  (balance checking))))

(deftest test-no-transfer-if-overdrawn
  (transfer checking savings 101)
  (is (= 100 (balance savings)))
  (is (= 100 (balance checking))))

(deftest test-concurrent-transfers
  (doall (pmap #(do
                  (dosync
                    (transfer checking savings %)
                    (transfer savings checking %)))
           (take 50 (repeat 2))))
  (is (= 100 @savings))
  (is (= 100 @checking)))



