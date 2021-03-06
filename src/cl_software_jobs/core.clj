(ns cl-software-jobs.core
  (:require [net.cgrand.enlive-html :as html]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [postal.core :as mailer]))

(def cl-url "http://boston.craigslist.org")
(def craigslist-clj-jobs-url (str cl-url "/search/sof?catAbb=sof&query="))
(def job-listing-selector [:div#toc_rows :div.content :p.row :span.pl :a])
(def cl-app-dir-name ".craigslist-jobs")

(defn- get-cl-app-dir
  "Returns a java.io.File object for the user's app directory."
  []
  (doto (java.io.File. (str (System/getProperty "user.home") java.io.File/separator cl-app-dir-name))
    (.mkdirs)))

(defn- get-email-settings-file
  "Returns a java.io.File object for the email settings file."
  []
  (java.io.File. (get-cl-app-dir) "email-settings.clj"))

(defn- get-query-file
  "Returns a java.io.File for the given query."
  [q]
  (java.io.File. (get-cl-app-dir) (str q ".clj")))

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
  (let [results-file (get-query-file q)]
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

(defn- save-query-result-urls
  "Saves the URL for each of the given Enlive :a tag query results in a file
  (whose name is the given query string with \".clj\" appended) in the app data
  directory."
  [q results]
  (let [urls (set (map #(get-in % [:attrs :href]) results))
        results-file (get-query-file q)]
    (spit results-file urls)))

(html/deftemplate email-template "templates/email-template.html"
  [query-str enlive-query-results]
  [:p#subject] (html/content (str "Query string: " query-str))
  [:ul [:li]] (html/clone-for [result enlive-query-results]
                              [:li :a] (html/content (:content result))
                              [:li :a] (html/set-attr :href (let [url (-> result :attrs :href)]
                                                              (if (.startsWith url "http")
                                                                url
                                                                (str cl-url url))))))

(defn- htmlify-query-results
  [query-str enlive-query-results]
  (apply str (email-template query-str enlive-query-results)))

(defn- email-query-results
  [query-str enlive-query-results]
  (let [email-settings (read-string (slurp (get-email-settings-file)))
        from (:from email-settings)
        to (:to email-settings)
        server-settings (dissoc email-settings :from :to)
        subject (let [prefix (if (empty? enlive-query-results) "No new " "")]
                  (str prefix "Craigslist jobs found for \"" query-str "\""))
        message (if (empty? enlive-query-results)
                  subject
                  (htmlify-query-results query-str enlive-query-results))]
    (mailer/send-message server-settings
                         {:from from :to to :subject subject
                          :body [{:type "text/html" :content message}]})))

(defn- run-craigslist-job-query
  "Performs the following side-effecting steps: 1) retrieves a collection of
enlive a-tag maps representing the results of the given Craigslist query, 2)
gets the set of these results that are newer than when the query was last run,
3) overwrites the old query results with the results of this new query, 4) sends
an email to the given recipient showing the set of newer results."
  [q]
  (let [enlive-results (get-newest-query-results q)]
    (save-query-result-urls q enlive-results)
    (email-query-results q enlive-results)))

(defn -main
  [& args]
  (let [query (apply str (interpose "+" args))]
    (run-craigslist-job-query query)))
