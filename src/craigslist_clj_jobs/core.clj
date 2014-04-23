(ns craigslist-clj-jobs.core
  (:require [net.cgrand.enlive-html :as html]))

(def cl-url "http://boston.craigslist.org")
(def craigslist-clj-jobs-url (str cl-url "/search/sof?catAbb=sof&query="))
(def job-listing-selector [:div#toc_rows :div.content :p.row :span.pl :a])

(defn- get-query-enlive-tags
  "Return a collection of Enlive :a tags representing the results of the given
query."
  [q]
  (let [query-url (str craigslist-clj-jobs-url q)
        results (-> (html/html-resource (java.net.URL. query-url))
                    (html/select job-listing-selector))]
    results))

(defn- run-craigslist-job-query
  "Performs the following side-effecting steps: 1) retrieves a collection of
enlive a-tag maps representing the results of the given Craigslist query, 2)
gets the set of these results that are newer than when the query was last run,
3) overwrites the old query results with the results of this new query, 4) sends
an email to the given recipient showing the set of newer results."
  [q]
  (let []))

(defn -main
  [& args]
  (let [query (apply str (interpose "+" args))]
    (run-craigslist-job-query query)))
