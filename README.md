# craigslist-clj-jobs

A Clojure library designed to retrieve the results of a parameterized Craigslist
search in the 'software' category.  The results will be emailed to a configured
email address.

## Usage

To configure email, create the file
'<home-dir>/.craigslist-jobs/email-settings.clj' and make sure to set the
permissions so that it is readable only by you.  This file must contain a single
Clojure map that has at least the following keys with appropriate values:

```clojure
{:from "yourname@example.com"	;; the address from which the email will be sent
 :to "someoneelse@example.com"}	;; the recipient's address
```

In addition to the above keys the map should contain keys for the email server
settings.  Please see [this page](https://github.com/drewr/postal) for details.
In short you may need to add some or all of the following keys:

```clojure
{:host "smtp.example.com"	;; the SMTP host name
 :port 465			;; the SMTP port
 :ssl :yes			;; if SSL is used
 :user "joe"			;; the user's email username
 :pass "sekrat"}		;; the user's email password
```

Run it from the command line using Leiningen with the following command:

    lein run -m craigslist-clj-jobs.core <search-term>

## License

Distributed under the Eclipse Public License, the same as Clojure.
