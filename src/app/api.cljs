(ns app.api
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]
            ["incognito-js" :as incognito-js]
            [async-await.core :refer [async await]]
            [app.storage :refer [state local accounts coins ptokens-temp coins-temp]]
            [goog.object :as g]
            [goog.string :as gstring :refer [format]]))


(defn get-ptokens-array [vector]
  (reset! ptokens-temp {})
  (let [id (atom 1)]
    (doseq [currency vector]
      (swap! ptokens-temp assoc (:TokenID currency) @id)
      (swap! id inc)))
  (swap! local assoc :ptokens @ptokens-temp))
;  (println "Ptokens array: " @ptokens))

(defn price-request []
    (go (let [response (<! (http/get "https://api.incognito.org/ptoken/list" {:with-credentials? false :headers {"Content-Type" "application/json"}}))
              coins-value (:Result (:body response))
              last-trade (first (:Result (:body response)))
              price (/ (:PriceUsd last-trade) (:PricePrv last-trade))]
          ;(swap! local assoc :prv-price price)
          (get-ptokens-array coins-value)
          (reset! coins-temp (into [] (reverse coins-value)))
          (swap! coins-temp conj {:ID 0
                                  :TokenID "ffd8d42dc40a8d166ea4848baf8b5f6e912ad79875f4373070b59392b1756c8f"
                                  :Symbol "PRV"
                                  :Name "Privacy"
                                  :Default true
                                  :PriceUsd (format "%.2f" price)
                                  :Verified true
                                  :PricePrv 1
                                  :volume24 12})
          (swap! local assoc :coins (into [] (reverse @coins-temp))))))
;        (println @coins))))

(defn init-incognito []
  (async
      (await (incognito-js/goServices.implementGoMethodUseWasm))
      (swap! state assoc :wasm-loaded true)
    (if (:backupkey @local)
      (.then
        (incognito-js/WalletInstance.restore (:backupkey @local) (:pw @local))
        #(g/set js/window "wallet" %))
      (.then
        (.init (incognito-js/WalletInstance.) (str (.getTime (js/Date.))) "default-wallet")
        #(do (g/set js/window "wallet" %) (swap! local assoc :backupkey (.backup % (-> % .-mnemonic)) :pw (-> % .-mnemonic)))))))
