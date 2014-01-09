# banking

A Clojure concurrency exercise.

Up and Running
------------

You have Leiningen [installed](https://github.com/technomancy/leiningen/blob/stable/README.md)

Create a new project

```bash
%> lein new banking
%> cd banking
```

To run tests add plugin ``lein-test-refresh`` by adding ``:plugins`` to ``project.clj``:

```clojure
(defproject banking "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :plugins [[com.jakemccrary/lein-test-refresh "0.1.2"]])
```

Now we run our tests

```bash
%> lein test-refresh
```

You should see the dummy test fail..

Editor: You may like [LightTable](http://www.lighttable.com/) but you can use any editor for this exercise.
  
Clojure [cheat sheet](http://clojure.org/cheatsheet) 

The Banker's Dilemma
-------

Create functions to credit, debit, and transfer between bank accounts. They must not be vulnerable to race conditions.

For example, if two threads try to credit the same account at the same time, the account balance must eventually be correctly incremented by BOTH credit amounts.

Concurrency in Clojure
-------

Concurrency support is built into Clojure. This exercise will cover **atoms** and **refs**.

You can have a look at [Clojure concurrency](http://clojure.org/concurrent_programming)


Withdrawals and deposits
-------

Data structures in Clojure are immutable. We can't just set up an account "variable" and have multiple threads access it. To support multiple threads manipulating the same data, Clojure provides atoms. An atom is a wrapper to hold a Clojure (immutable) data structure. 

It's called an "atom" because any write to the data is atomic; that is, one thread will have exclusive access to the data whilst writing. The programmer doesn't have to manage locks.

You define an atom like this (def my-atom (**atom** 1000))
*Creates an atom with an integer value of 1000.*

You reference the value of an atom using the **@** operator, e.g. @my-atom.

You change the value of an atom by passing a function to **swap!** (swap! my-atom inc) 
*Increments whatever value is in my-atom.*

You can force a value into an atom by passing a value to **reset!**, e.g. (reset! my-atom 42).

See the docs for [atoms](http://clojure.org/atoms).

The underlying data structure can be any Clojure data structure; int, list, vector, map, etc. We'll make our bank account be an int, referring to the balance of the account:

```clojure

(def my-checking-account (atom 0))

```

We have an account; let's test that its balance is zero. Tests are where? Under the test folder, naturally.

We can replace the generated, failing test with our own test. The namespace (ns) function at the top of the file is still good.

``banking\test\banking\core_test.clj``
```clojure
(ns banking.core-test
  (:require [clojure.test :refer :all]
            [banking2.core :refer :all]))
            
(def my-checking-account (atom 0))

(deftest test-balance
  (is (= 0 (balance my-checking-account))))
  
```

When we save the file, leiningen will run the tests. This won't run because we haven't defined the function "balance". See if you can define it. 

Source code goes where? Under the src folder. You can replace the auto-generated "foo" function.


``banking\src\banking\core.clj``
```clojure
(ns banking.core)

(defn balance [account]
  @account)
  
```

The parameter "account" is an atom, so we reference its value using the @ operator. 

Now when we save, our test runs and passes.

How about depositing money into an account? Back in ``core_test.clj``:

```clojure
...

(deftest test-credit
  (credit my-checking-account 60)
  (is (= 60 (balance my-checking-account))))
  
```

Knowing how to use swap!, can you define the function credit?


Here's one way:

``core.clj``
```clojure
...

(defn credit [account amount]
  (swap! account #(+ % amount)))
```

We're passing swap! the account and an anonymous function that will add "amount" to whatever balance is currently in the atom.

This test passes, but we've broken our first test. It seems the tests are using the same atom and interfering with each other.

We want to set up an account in a known state **before each test**. 

Clojure-test support "fixtures" for test setup. Note: A fixture will apply to all tests in a namespace, or none. There are no classes in Clojure, so functions are organized by namespace.

```clojure
(def checking (atom 0))
(def savings (atom 0))

(defn my-fixture [f]
  (reset! checking 100)
  (reset! savings 100)
  (f))

(use-fixtures :each my-fixture)

...

```

Now we can update our tests to use the accounts created in setup. We can delete my-checking-account.

```clojure
...

(deftest test-balance
  (is (= 100 (balance checking))))
  
(deftest test-credit
  (credit checking 60)
  (is (= 160 (balance checking))))
```

Yay, our tests pass again!

We should pull those definitions out into a function, though, to make sure we're really getting fresh accounts before each test. Let's change 

```clojure
(def checking (atom 0))
(def savings (atom 0))

...
```

to

```clojure
(def checking (make-account))
(def savings (make-account))

...
```

and define make-account in the source file:

```clojure
(defn make-account []
  (atom 0))
  
...
```

OK. Good. Any more refactors? The credit function could look a little prettier. swap! allows a more elegant syntax:

```clojure
...

(defn credit [account amount]
  (swap! account + amount))
```

Moving on to write the test for debit. 

```clojure
...

(deftest test-debit
  (debit checking 100)
  (is (= 0 (balance checking))))
```

To implement debit, we could use the credit function we just wrote....

```clojure
...

(defn debit [account amount]
  (credit account (- amount)))
```

Tests pass.

We shouldn't allow overdraft. Let's expect an exception:

```clojure
...

(deftest test-overdraw-exception
  (is (thrown? Exception
        (debit checking 101))))
```

You can look up how to throw an exception in Clojure, and implement this behavior in the debit function. 

Got it? Here's what I have:

```clojure
...

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

Transfers
-------

Let's get to the fun part! Writing the test for transfer:

```clojure
...

(deftest test-transfer
  (transfer savings checking 25)
  (is (= 75 (balance savings)))
  (is (= 125  (balance checking))))
```

Can you write transfer in terms of debit and credit?

```clojure
...

(defn transfer [from to amount]
  (debit from amount)
  (credit to amount))
```

We don't want to allow the transfer if it would cause an overdraft.

```clojure
...

(deftest test-no-transfer-if-overdrawn
  (transfer checking savings 101)
  (is (= 100 (balance savings)))
  (is (= 100 (balance checking))))
```

Can you implement it?

```clojure
...

(defn transfer [from to amount]
  (when (>= (balance from) amount)
    (debit from amount)
    (credit to amount)))
```


We're rocking now. 

Naturally, we want to show off our excellent concurrency support. Let's write a parallel transfer test. We create transactions that transfer money between the accounts; in the end our total balance of both accounts should be the same as when we started. 

```clojure
...

(deftest test-concurrent-transfers
  (doall (pmap #(do
                  (transfer checking savings %)
                  (transfer savings checking %)
                  )
           (take 100 (repeatedly #(rand-int 25)))))
  (is (= 200 (+ (balance savings) (balance checking)))))
```

Looks great! Let's make it more realistic by adding some long-running operations inside our transfer function.

```clojure
...

(defn transfer [from to amount]
  (when (>= (balance from) amount)
    (Thread/sleep 10)
    (debit from amount)
    (credit to amount)))
```

Failing test? Insufficient Funds Exception? WAIT WHAT HAPPENED???!!??? We were supposed to get concurrency for free. :-( :-( :-( Time to stop and think about this. Can you see why our implementation of transfer is unsafe?

Hmmm....

Yes....

That's right. Atoms protect us when we're synchronizing changes to **one** piece of data. Here we have **two** pieces of data that we need to keep mutually consistent. 

Atoms aren't enough for this.

We need TRANSACTIONS.

Transactions
-------

This is when we need to carefully read the 4th paragraph at [Clojure concurrency](http://clojure.org/concurrent_programming). 

Basically, use refs instead of atoms, and any (write) access to a ref MUST be within a transaction (dosync).

You define a ref like this (def my-ref (**ref** 1000))
*Creates a ref with an integer value of 1000.*

You reference the value of a ref using the **@** operator, e.g. @my-ref.

You change the value of a ref by passing a function to **alter** (alter my-ref inc) 
*Increments whatever value is in my-ref.*

You can force a value into a ref by passing a value to **ref-set**, e.g. (ref-set my-ref 42).

(Write) accesses to a ref MUST be within a transaction. You indicate a transaction like this:

```clojure
(dosync 
  (do-stuff-here) 
  (more-stuff-if-you-want)
  ... )
```

See the docs for [refs](http://clojure.org/refs).

OK, let's start changing our implementation to use refs. We can comment out our failing test for now:

```clojure
...

;(deftest test-concurrent-transfers
;  (doall (pmap #(do
;                  (transfer checking savings %)
;                  (transfer savings checking %)
;                  )
;           (take 100 (repeatedly #(rand-int 25)))))
;  (is (= 200 (+ (balance savings) (balance checking)))))
```

First we change make-account to return a ref

```clojure

(defn make-account []
  (ref 0))
  
```

We need to change swap! to alter. Similar function, but swap! works on atoms, and alter works on refs.

```clojure

(defn credit [account amount]
  (alter account +  amount))

```

And in our tests, we need to change reset! to ref-set. Those Frenchmen, they have a different word for **everything**.

```clojure

(defn my-fixture [f]
  (ref-set checking 100)
  (ref-set savings 100)
  (f))
  
```

Now we get an java.lang.IllegalStateException: No transaction running. This is what we expected. We know refs can only be accessed within transactions. We'll make each of our three functions, credit, debit, and transfer, be a transaction.

```clojure
...

(defn credit [account amount]
  (dosync
    (alter account #(+ % amount))))

(defn debit [account amount]
  (dosync
    (when (> amount (balance account))
      (throw (Exception. "Insufficient Funds")))
    (credit account (- amount))))

(defn transfer [from to amount]
  (dosync
    (when (>= (balance from) amount)
      (Thread/sleep 10)
      (debit from amount)
      (credit to amount))))
      
```

Ah yes, we also have to put the test initialization inside a transaction. Aren't stack traces helpful?

```clojure
(defn my-fixture [f]
  (dosync
    (ref-set checking 100)
    (ref-set savings 100))
  (f))
  
...
```

Refactor complete! Our tests are passing. Now we can uncomment that last test...

```clojure
...

(deftest test-concurrent-transfers
  (doall (pmap #(do
                  (transfer checking savings %)
                  (transfer savings checking %)
                  )
           (take 100 (repeatedly #(rand-int 25)))))
  (is (= 200 (+ (balance savings) (balance checking)))))
```

And it passes! Cookies and milk all around!


Conclusion
-------

Thank you @richhickey for providing such an elegant implementation of [STM](http://en.wikipedia.org/wiki/Software_transactional_memory) built into the language. 

If you liked this exercise, why not try your hand at the [Sleeping Barber](http://en.wikipedia.org/wiki/Sleeping_barber_problem) concurrency problem? 

![alt text](http://clojure.org/space/showimage/clojure-icon.gif "Clojure")

