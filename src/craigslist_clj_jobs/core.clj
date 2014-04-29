(ns craigslist-clj-jobs.core
  (:require [net.cgrand.enlive-html :as html]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

(def cl-url "http://boston.craigslist.org")
(def craigslist-clj-jobs-url (str cl-url "/search/sof?catAbb=sof&query="))
(def job-listing-selector [:div#toc_rows :div.content :p.row :span.pl :a])
(def cl-app-dir-name ".craigslist-jobs")

(defn- get-query-enlive-tags
  "Return a collection of Enlive :a tags representing the results of the given
query. An Enlive :a tag is a map with keys :tag (and value :a), :attrs (a map of
  attributes one of which will be :href) and finally :content which will be the
  text found within the :a tag."
  [q]
  (let [query-url (str craigslist-clj-jobs-url q)
        results (-> (html/html-resource (java.net.URL. query-url))
                    (html/select job-listing-selector))]
    results))

(defn- get-previous-query-results-urls
  "Returns the set of query result URLs that were obtained from the previous run
  of this program."
  [q]
  (let [cl-app-dir (doto (java.io.File. (str (System/getProperty "user.home") java.io.File/separator cl-app-dir-name))
                     (.mkdirs))
        results-file (java.io.File. cl-app-dir (str q ".clj"))]
    (if (.exists results-file)
      (with-open [pbrdr (java.io.PushbackReader. (io/reader results-file))]
        (edn/read pbrdr))
      #{})))

(defn- get-newest-query-results
  "Returns a collection of Enlive data structures representing the links of the
  search results that are new than those from the previous run of this
  function with the given query parameter."
  [q]
  (let [previous-urls (get-previous-query-results-urls q)]
    ;; return the current results but filter out those results whose URL is in
    ;; the set of previous-urls
    (->> (get-query-enlive-tags q)
         (filter (fn [{{url :href} :attrs}]
                   (not (previous-urls url)))))))

(defn- htmlify-query-results
  [r])

(defn- run-craigslist-job-query
  "Performs the following side-effecting steps: 1) retrieves a collection of
enlive a-tag maps representing the results of the given Craigslist query, 2)
gets the set of these results that are newer than when the query was last run,
3) overwrites the old query results with the results of this new query, 4) sends
an email to the given recipient showing the set of newer results."
  [q]
  (let [enlive-results (get-newest-query-results q)
        html-text (htmlify-query-results enlive-results)]))

(defn -main
  [& args]
  (let [query (apply str (interpose "+" args))]
    (run-craigslist-job-query query)))
