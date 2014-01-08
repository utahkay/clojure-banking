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

Here's how I did it:

```clojure

(defn balance [account]
  @account)
  
```

The parameter "account" is an atom, so we can reference its value using the @ operator.


Let's test depositing money into an account. Let's call that "credit."

```clojure

(deftest test-credit
  (credit my-checking-account 6)
  (is (= 6 (balance checking))))
  
```

