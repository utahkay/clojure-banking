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
