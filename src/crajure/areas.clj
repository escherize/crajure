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
       set))

(def areas (memoize generate-areas))


(defn area-map []
  (assoc
      (into {} (map (fn [i] [(keyword i) i]) (areas)))
    :all (vec (areas))))
