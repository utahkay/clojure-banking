(ns banking.core)

(defn make-account []
  (atom 0))

(defn balance [account]
  @account)

(defn credit [account amount]
  (swap! account #(+ % amount)))

(defn debit [account amount]
  (when (> amount (balance account))
    (throw (Exception. "Insufficient Funds")))
  (credit account (- amount)))

(defn transfer [from to amount]
  (when (>= (balance from) amount)
    (Thread/sleep 10)
    (debit from amount)
    (credit to amount)))






