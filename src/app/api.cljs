(ns app.api
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom]]
            [alandipert.storage-atom :refer [local-storage]]
            [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]
            ["incognito-js" :as incognito-js]
            [async-await.core :refer [async await]]
            [app.storage :refer [state local accounts]]
            [goog.object :as g]))

(defn price-request []
  (go (let [response (<! (http/get "https://api.incognito.org/ptoken/list" {:with-credentials? false :headers {"Content-Type" "application/json"}}))
            ptokens (:Result (:body response))
            price (/ (:PriceUsd (first ptokens)) (:PricePrv (first ptokens)))]
        (do (swap! state assoc :prv-price price)
            (swap! state assoc :ptokens (js->clj ptokens))))))

(defn init-incognito []
  (async
   (let []
     (await (incognito-js/goServices.implementGoMethodUseWasm))
     (swap! state assoc :wasm-loaded true))
   (if (:backupkey @local)
       (.then
        (incognito-js/WalletInstance.restore (:backupkey @local) (:pw @local))
        #(g/set js/window "wallet" %))
       (.then
         (.init (incognito-js/WalletInstance.) (str (.getTime (js/Date.))) "default-wallet")
         #(do (g/set js/window "wallet" %) (swap! local assoc :backupkey (.backup % (-> % .-mnemonic)) :pw (-> % .-mnemonic)))))))
