(ns crajure.core
  (:require [clojure.string :as str]
            [net.cgrand.enlive-html :as html]
            [crajure.util :as u]))


(defn query+area->url [area section query-str]
  (format "http://%s.craigslist.org/search/%s?s=__PAGE_NUM__&query=%s&sort=pricedsc"
          area section query-str))

(defn search-str->query-str [search-str]
  (str/replace search-str " " "%20"))

(defn item-count->page-count [num-selected-string]
  (-> num-selected-string read-string (/ 100) int inc))

(defn get-num-pages [query-str section area]
  (let [url (-> (query+area->url area section query-str)
                (str/replace "s=__PAGE_NUM__&" ""))
        page (u/fetch-url url)
        num-selected-large (-> (html/select page [:a.totalcount])
                               first :content first)
        num-selected (or num-selected-large
                         (-> (html/select page [:span.button.pagenum])
                             html/texts first (str/split #" ") last))]
    (item-count->page-count num-selected)))

(defn page->prices [page]
  (->> (html/select page [:.price])
       (map (comp u/dollar-str->int first :content))))

(defn page->titles [page]
  (->> (html/select page [:span.pl :a])
       (map (comp first :content))))

(defn page->dates [page]
  (->> (html/select page [:time])
       (map (comp :datetime :attrs))))

(defn page->item-urls [page area]
  (->> (html/select page [:span.pl :a])
       (map (comp :href :attrs))
       (map (fn [u] (str "http://" area
                         ".craigslist.org" u)))))

(defn page->regions [page]
  (->> (html/select page [:span.pnr :small])
       (map (comp str/trim first :content))
       (map (fn [s] (apply str (drop-last (rest s)))))))

(defn page->categories [page]
  (->> ( html/select page [:span.l2 :a.gc])
       (map (comp first :content))))

(defn ->item-map [area price title date item-url region category]
  {:price price
   :title title
   :date date
   :region region
   :item-url item-url
   :category category})

(defn page+area->item-map [page area]
  (map ->item-map
       (repeat area)
       (page->prices page)
       (page->titles page)
       (page->dates page)
       (page->item-urls page area)
       (page->regions page)
       (page->categories page)))

(defn cl-item-seq [area section query-str]
  (let [page-count (get-num-pages query-str section area)
        page-range (map #(-> % (* 100) str) (range 0 page-count))]
    (->
     (for [page-name page-range]
       (let [url (query+area->url area section query-str)
             page (u/fetch-url url)]
         (page+area->item-map page area)))
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
    items))
