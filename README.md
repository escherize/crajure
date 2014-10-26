crajure
=======

Craigslist-as-a-backend for clojure.

This project is the read half of a full craigslist Library.

We're using it for data vis, among other things.

[![Clojars Project](http://clojars.org/crajure/latest-version.svg)](http://clojars.org/crajure)

usage
---
To search for fixie bikes in san francisco:

```{clojure}
(query-cl {:query "fixie bike"
           :area "sfbay"
           :section :for-sale})
```
```
;=> ({:price 2500,
     :title "2013 Giant Glory 2 Medium *price drop*",
     :date "2014-10-22 21:20",
     :region "morgan hill",
     :item-url "http://sfbay.craigslist.org/sby/bik/4713638395.html",
     :category "bicycles - by owner"}
    {:price 2500,
     :title "FS: Cinelli Mash Original Bolt Complete 53cm",
     :date "2014-10-20 11:57",
     :region "san jose south",
     :item-url "http://sfbay.craigslist.org/sby/bik/4681252594.html",
     :category "bicycles - by owner"} ...)
```
