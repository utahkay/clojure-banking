(ns banking.core-test
  (:require [clojure.test :refer :all]
            [banking.core :refer :all]))

(def accounts (atom {:checking 10 :savings 10}))

(defn my-fixture [f]
  (reset! accounts {:checking 10 :savings 10})
  (f))

(use-fixtures :each my-fixture)

(deftest credit-amount-to-account
  (swap! accounts credit :checking 6)
  (is (= 16 (get-balance @accounts :checking))))

(deftest debit-amount-from-account
  (swap! accounts debit :checking 6)
  (is (= 4 (get-balance @accounts :checking))))

(deftest attempt-to-overdraw-throws-exception
  (is (thrown? Exception
        (debit @accounts :checking 11))))

(deftest concurrent-access
  (doall (pcalls
           #(swap! accounts credit :checking 4)
           #(swap! accounts debit :checking 5)
           #(swap! accounts credit :checking 4)
           #(swap! accounts debit :checking 5)
          ))
  (is (= 10 (get-balance @accounts :savings)))
  (is (= 8 (get-balance @accounts :checking))))

(deftest transfer-from-to
  (transfer accounts :checking :savings 5)
  (is (= 15 (get-balance @accounts :savings)))
  (is (= 5  (get-balance @accounts :checking))))

(deftest does-not-transfer-if-overdrawn
  (transfer accounts :checking :savings 15)
  (is (= 10 (get-balance @accounts :savings)))
  (is (= 10 (get-balance @accounts :checking))))

;(deftest concurrent-transfers
;  (doall (pmap #(do
;                  (transfer accounts :checking :savings %)
;                  (transfer accounts :savings :checking %))
;           (take 100 (repeatedly #(rand-int 5)))))
;  (is (= 10 (get-balance @accounts :savings)))
;  (is (= 10 (get-balance @accounts :checking))))



