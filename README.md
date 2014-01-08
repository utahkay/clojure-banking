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

It's called an "atom" because any write to the data is atomic, that is, one thread will have exclusive access to the data whilst writing. The programmer doesn't have to manage locks.

You define an atom as follows. Again, the underlying data structure can be any Clojure data structure; int, list, vector, map, etc. This atom is an int:

```clojure

(def my-atom (atom 0))

```
