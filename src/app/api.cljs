(ns app.api
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]))

(def prv-price (atom 0))

(defn price-request []
  (go (let [response (<! (http/get "https://api.incognito.org/ptoken/list" {:with-credentials? false :headers {"Content-Type" "application/json"}}))
            last-trade (first (:Result (:body response)))
            price (/ (:PriceUsd last-trade) (:PricePrv last-trade))]
        (reset! prv-price price))))
