(ns crajure.areas
  (:require [net.cgrand.enlive-html :as html]
            [crajure.util :as u]))

(defonce area-atom (atom #{}))

(defn generate-areas []
  (->> (html/select
        (u/fetch-url "https://www.craigslist.org/about/sites")
        [:li :> :a])
       (map (comp :href :attrs))
       (map #(-> (re-find #"\/\/([a-z]*)" %) second))
       distinct
       (filter #(u/has-page? (str "http://" % ".craigslist.org")))
       set))

(defn areas []
  (if-let [areas-found @area-atom] 
    areas-found
    (do 
      (swap! area-atom (fn [x] (conj x (generate-areas))))
      (areas))))

