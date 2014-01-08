(ns banking.core-test
  (:require [clojure.test :refer :all]
            [banking.core :refer :all]))

(def checking (make-account))
(def savings (make-account))

(defn my-fixture [f]
  (reset! checking 10)
  (reset! savings 10)
  (f))

(use-fixtures :each my-fixture)

(deftest test-credit
  (credit checking 6)
  (is (= 16 (balance checking))))

(deftest test-debit
  (debit checking 6)
  (is (= 4 (balance checking))))

(deftest test-overdraw-exception
  (is (thrown? Exception
        (debit checking 11))))

(deftest test-concurrent-credits-debits
  (doall (pmap #(do
                  (credit checking (+ % 1))
                  (debit checking %))
               (take 100 (repeat 5))))
  (is (= 110 (balance checking))))

(deftest test-transfer
  (transfer savings checking 5)
  (is (= 5 (balance savings)))
  (is (= 15  (balance checking))))

(deftest test-no-transfer-if-overdrawn
  (transfer checking savings 15)
  (is (= 10 (balance savings)))
  (is (= 10 (balance checking))))

(deftest test-concurrent-transfers
  (doall (pmap #(do
                  (transfer checking savings %)
                  (transfer savings checking %)
                  )
           (take 100 (repeatedly #(rand-int 5)))))
  (is (= 10 (balance savings)))
  (is (= 10 (balance checking))))



