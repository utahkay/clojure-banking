# banking

A Clojure concurrency exercise

Up and Running
-------
* Install [Leiningen](http://leiningen.org/)
 * Put shell script/batch file somewhere on your path 
 * Run lein self-install
* Create new project
 * lein new banking
* In the created banking folder there is a project.clj. Edit project.clj to add "test-refresh" plugin for continuous test.
 * add :plugins line as follows

```clojure
(defproject banking "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :plugins [[com.jakemccrary/lein-test-refresh "0.1.2"]])
```

* At the command line cd to your new project folder (banking)
* lein test-refresh
 
You should see 1 failing test. Leiningen generated a skeleton project for you with one sample (failing) test.


The Problem
-------

Create functions to credit, debit, and transfer between bank accounts. They must not be vulnerable to race conditions.

For example, if two threads try to credit the same account at the same time, the account balance must eventually be correctly incremented by BOTH credit amounts.

Concurrency in Clojure
-------

Concurrency support is built into Clojure. This exercise will cover **atoms** and **refs**.

For now, just see [Clojure concurrency](http://clojure.org/concurrent_programming)


Creating an account
-------

Data structures in Clojure are immutable. We can't just set up an account "variable" and have multiple threads access it. To support multiple threads manipulating the same data, Clojure provides atoms. An atom is a wrapper to hold a Clojure (immutable) data structure. 

It's called an "atom" because any write to the data is atomic; that is, one thread will have exclusive access to the data whilst writing. The programmer doesn't have to manage locks.

You can reference the value of an atom using the @ operator, e.g. @my-atom.

You can change the value of an atom by passing a function to **swap!**, e.g. (swap! my-atom inc) will increment whatever value is in my-atom (assuming it's an integer value). See the docs for  [atoms](http://clojure.org/atoms) and [swap!](http://clojuredocs.org/clojure_core/clojure.core/swap!).

You define an atom as follows. Again, the underlying data structure can be any Clojure data structure; int, list, vector, map, etc. We'll make our bank accounts be ints, referring to the balance of the account:

```clojure

(def my-checking-account (atom 0))

```

Let's start simple, and test that the balance of our account is zero. 

```clojure

(deftest test-balance
  (is (= 0 (balance checking))))
  
```

This won't run because we haven't defined the function "balance". See if you can define it.



OK here's the answer:

```clojure

(defn balance [account]
  @account)
  
```

The parameter "account" is an atom, so we can reference its value using the @ operator.


Let's test depositing money into an account. Let's call that function "credit." Credit will take as parameters the account, and the amount to deposit.

```clojure

(deftest test-credit
  (credit my-checking-account 60)
  (is (= 60 (balance checking))))
  
```

Knowing how to use swap!, can you define the function credit?


Here's one way:

```clojure

(defn credit [account amount]
  (swap! account #(+ % amount)))
  
```

We're passing swap! the account and an anonymous function that will add "amount" to whatever balance is currently in the atom.

swap! allows a more elegant way to write this, however:

```clojure

(defn credit [account amount]
  (swap! account + amount))

```

Either way, the test should pass. Moving on to write the test for debit. 

```clojure

(deftest test-debit
  (debit my-checking-account 60)
  (is (= -60 (balance checking))))

```

Hmm, we probably shouldn't allow negative balances. But let's implement this anyway. The implementation should be quite similar to credit.

```clojure

(defn debit [account amount]
  (swap! account - amount))
  
```


(Are the tests interfering with each other at this point?)

It might be easier if we set up an account in a known state before each test. You can force a value into an atom using reset!, e.g. (reset! my-atom 5)

Clojure-test support "fixtures" for test setup. Note: A fixture will apply to all tests in a namespace, or none. There are no classes in Clojure, so functions are organized by namespace.

```clojure

(def checking (atom 0))
(def savings (atom 0))

(defn my-fixture [f]
  (reset! checking 100)
  (reset! savings 100)
  (f))

(use-fixtures :each my-fixture)

```

Now we can update our tests to use the accounts created in setup. We can delete my-checking-account.

```clojure

(deftest test-credit
  (credit checking 60)
  (is (= 160 (balance checking))))

(deftest test-debit
  (debit checking 60)
  (is (= 40 (balance checking))))
  
```

Yay, our tests pass again!

We shouldn't allow overdraft. Let's write a test that expects an exception if this happens:

```clojure

(deftest test-overdraw-exception
  (is (thrown? Exception
        (debit checking 110))))
        
```

You can look up how to throw an exception in Clojure, and implement this behavior. 

Got it? Here's what I have:

```clojure

(defn debit [account amount]
  (when (> amount (balance account))
    (throw (Exception. "Insufficient Funds")))
  (credit account (- amount)))
  
```

Now I want to demonstrate that we have gotten our concurrency for free, just by using an atom. Here's a test that credits our checking account with $6 and debits it $5, doing this 100 times. Thus we're $1 ahead each time, so after all is said and done our account balance should be $100 greater than when we started. (Hopefully this is how we manage our moeny in real life...)

All the transactions happen in parallel through the magic of pmap.

```clojure

(deftest test-concurrent-credits-debits
  (doall (pmap #(do
                  (credit checking (+ % 1))
                  (debit checking %))
               (take 100 (repeat 5))))
  (is (= 200 (balance checking))))
  
```

This test passes because we ensured synchronization of our data value, by using an atom.

