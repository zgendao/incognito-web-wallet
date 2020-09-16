(ns app.wallet
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom]]
            [app.storage :refer [state accounts coins local ptokens accounts-temp]]
            [goog.object :as g]
            [async-await.core :refer [async await]]
            [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]))

(defn wallet [] (g/get js/window "wallet"))

(defn get-prv [account]
  (.then
   (-> account .-nativeToken (.getAvaiableBalance))
   #(swap! local assoc-in [:saved-balances :prv (-> account .-name)] (/ % 1000000000))))

(defn follow-token [account]
  (async
    (await (-> account (.followTokenById "teszt")))
    (swap! local assoc :backupkey (.backup (wallet) (-> (wallet) .-mnemonic)))))

(defn getprivacytokenslist [acc]
  (go (let [ptoken-list (<! (http/post "https://fullnode.incognito.org" {:json-params {:jsonrpc "1.0" :method "getlistprivacycustomtokenbalance" :params [(-> acc .-key .-keySet .-privateKeySerialized)] :id 1} :with-credentials? false :headers {"Content-Type" "application/json"}}))]
           (swap! local assoc-in [:saved-balances (-> acc .-name)] (get-in ptoken-list [:body :Result :ListCustomTokenBalance]))
           (print ptoken-list)
           10)))


(defn init-wallet []
  (async
    ;(swap! state assoc :accounts (.getAccounts (.-masterAccount (wallet))))
    (reset! accounts-temp [])
    (print @local)
    (doseq [acc (.getAccounts (.-masterAccount (wallet)))]
        (if (get-in @local [:saved-balances :prv (-> acc .-name)])
          (get-prv acc)
          (await (get-prv acc)))
        (await (getprivacytokenslist acc))
        (swap! local assoc-in [:balances (-> acc .-name)] [{:id 0 :amount (get-in @local [:saved-balances :prv (-> acc .-name)])}])
        (doseq [ptoken (get-in @local [:saved-balances (-> acc .-name)])]
             (swap! local assoc-in [:balances (-> acc .-name)] (conj (get-in @local [:balances (-> acc .-name)]) {:id (get-in @local [:ptokens (:TokenID ptoken)]) :amount (/ (:Amount ptoken) 1000000000)})))
        (swap! accounts-temp conj {:name (.-name acc)
                                   :keys {:incognito (.-paymentAddressKeySerialized (.-keySet (.-key acc)))
                                          :public (.-publicKeySerialized (.-keySet (.-key acc)))
                                          :private (.-privateKeySerialized (.-keySet (.-key acc)))
                                          :readonly (.-viewingKeySerialized (.-keySet (.-key acc)))
                                          :validator (.-validatorKey (.-keySet (.-key acc)))}
                                   :coins (get-in @local [:balances (-> acc .-name)])}))
    (swap! local assoc :accounts @accounts-temp)
    (print "most init")))

(defn create-backup []
  (swap! local assoc :backupkey (.backup (wallet) (-> (wallet) .-mnemonic)))
  (swap! state assoc :render (.getTime (js/Date.))))