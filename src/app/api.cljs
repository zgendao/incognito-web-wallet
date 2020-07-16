(ns app.api
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom]]
            [alandipert.storage-atom :refer [local-storage]]
            [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]
            ["incognito-js" :as incognito-js]
            [async-await.core :refer [async await]]
            [app.state :refer [state]]))

(defn price-request []
  (go (let [response (<! (http/get "https://api.incognito.org/ptoken/list" {:with-credentials? false :headers {"Content-Type" "application/json"}}))
            last-trade (first (:Result (:body response)))
            price (/ (:PriceUsd last-trade) (:PricePrv last-trade))]
        (swap! state assoc :prv-price price))))

(defn init-incognito []
  (async
   (let []
     (await (incognito-js/goServices.implementGoMethodUseWasm))
     (swap! state assoc :wasm-loaded true))))

(def ^:export wallet (incognito-js/WalletInstance.))
