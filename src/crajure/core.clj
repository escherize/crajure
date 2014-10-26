(ns crajure.core
  (:require [clojure.string :as str]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as str]))

(defn has-page? [url]
  (boolean
   (fetch-url url)))

(defonce areas
  (->> (html/select
        (fetch-url "https://www.craigslist.org/about/sites")
        [:li :> :a])
       (map (comp :href :attrs))
       (map #(-> (re-find #"\/\/([a-z]*)" %) second))
       distinct
       (filter #(has-page? (str "http://" % ".craigslist.org")))
       sort))

(defn query+area->url [area section query-str]
  (format "http://%s.craigslist.org/search/%s?s=__PAGE_NUM__&query=%s&sort=pricedsc"
          area section query-str))

(defn search-str->query-str [search-str]
  (str/replace search-str " " "%20"))

(defn item-count->page-count [num-selected-string]
  (-> num-selected-string read-string (/ 100) int inc))

(defn get-num-hits [query-str section area]
  (let [url (-> (query+area->url area section query-str)
                (str/replace "s=__PAGE_NUM__&" ""))
        page (fetch-url url)
        num-selected-large (-> (html/select page [:a.totalcount])
                               first :content first)
        num-selected (or num-selected-large
                         (-> (html/select page [:span.button.pagenum])
                             html/texts first (str/split #" ") last))]
    (item-count->page-count num-selected)))

(defn dollar-str->int [x-dollars]
  (try (->> x-dollars
            rest
            (apply str)
            read-string)
       (catch Exception e (str "dollar-str->int got: "
                               x-dollars ", not a dollar amount."))))

(defn cl-item-seq [area section query-str]
  (let [page-count (get-num-hits query-str section area)
        page-range (map #(-> % (* 100) str) (range 0 page-count))]
    (->
     (for [page-name page-range]
       (let [url (query+area->url area section query-str)
             page (fetch-url url)
             prices (->> (html/select page [:.price])
                         (map (comp dollar-str->int first :content)))
             titles (->> (html/select page [:span.pl :a])
                         (map (comp first :content)))
             dates (->> (html/select page [:time])
                        (map (comp :datetime :attrs)))
             item-urls (->> (html/select page [:span.pl :a])
                            (map (comp :href :attrs))
                            (map (fn [u] (str "http://" area
                                              ".craigslist.org" u))))
             regions (->> (html/select page [:span.pnr :small])
                          (map (comp str/trim first :content))
                          (map (fn [s] (apply str (drop-last (rest s))))))
             categories (->> ( html/select page [:span.l2 :a.gc])
                             (map (comp first :content)))]
         (map (fn [area price title date region item-url category]
                {:price price
                 :title title
                 :date date
                 :region region
                 :item-url item-url
                 :category category})
              (repeat area) prices titles dates regions item-urls categories)))
     concat
     first)))

(defn get-section-code
  [section-key]
  (get {:community "ccc"
        :events "eee"
        :for-sale "sss"
        :gigs "ggg"
        :housing "hhh"
        :jobs "jjj"
        :personals "ppp"
        :resumes "rrr"
        :services "bbb"}
       section-key
       "sss"))

(defn query-cl
  "where query map contains a map of
  :query - a string like \"fixie bikes\"
  :area - a prefix like sfbay
  :section - a key from get-section-code
  ;          i.e. :for-sale
  "
  [{:keys [query area section] :as query-map}]
  (let [terms (search-str->query-str query)
        section-key (get-section-code section)
        items (cl-item-seq area section-key terms)]
    {:items items
     :area area
     :section section}))

(defn round-to-nearest [to from]
  (let [add (/ to 2)
        divd (int (/ (+ add from) to))]
    (* divd to)))


