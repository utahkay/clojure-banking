(ns banking.core)

(defn make-account []
  (ref 0))

(defn balance [account]
  (dosync @account))

(defn credit [account amount]
  (dosync
    (alter account + amount)))

(defn debit [account amount]
  (dosync
    (when (> amount (balance account))
      (throw (Exception. "Insufficient Funds")))
    (alter account - amount)))

(defn transfer [from to amount]
  (dosync
    (when (>= (balance from) amount)
      (Thread/sleep 10)
      (debit from amount)
      (credit to amount))))






