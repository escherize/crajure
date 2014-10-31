(ns crajure.util
  (:require [net.cgrand.enlive-html :as html]))

(defn fetch-url [url & properties]
  (with-open [inputstream (-> (java.net.URL. url)
                              .openConnection
                              (doto (.setRequestProperty
                                     "User-Agent" "Mozilla/5.0"))
                              .getContent)]
    (html/html-resource inputstream)))

(defn has-page? [url]
  (boolean
   (fetch-url url)))


(defn dollar-str->int [x-dollars]
  (try (->> x-dollars
            rest
            (apply str)
            read-string)
       (catch Exception e (str "dollar-str->int got: "
                               x-dollars ", not a dollar amount."))))

(defn round-to-nearest [to from]
  (let [add (/ to 2)
        divd (int (/ (+ add from) to))]
    (* divd to)))

(defn map-or-1
  "like map, but can also act on a single item
  (map inc 1)   ;=> [2]
  (map inc [1]) ;=> [2]"
  [f coll]
  (if (seq? coll)
    (map f coll)
    (map f [coll])))

(defn ->flat-seq
  [x]
  (flatten 
   (if (seq? x) 
     (vec x) 
     (vector x))))

