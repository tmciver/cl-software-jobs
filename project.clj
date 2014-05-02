(defproject craigslist-clj-jobs "0.1.0-SNAPSHOT"
  :description "An HTTP client to retrieve a list of Clojure jobs listed on
Craigslist."
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-http "0.9.1"]
                 [enlive "1.1.5"]
                 [com.draines/postal "1.11.1"]]
  :resource-paths ["src/resources"]
  :main craigslist-clj-jobs.core)
