(ns banking.core)

(defn get-balance [accounts account-name]
  (accounts account-name))

(defn credit [accounts account-name amount]
  (update-in accounts [account-name] #(+ % amount)))

(defn debit [accounts account-name amount]
  (when (> amount (accounts account-name))
    (throw (Exception. "Insufficient Funds")))
  (credit accounts account-name (- amount)))

(defn transfer [accounts-atom from to amount]
  (when (>= (get-balance @accounts-atom from) amount)
    (swap! accounts-atom debit from amount)
    (swap! accounts-atom credit to amount)))






