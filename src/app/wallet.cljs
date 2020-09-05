(ns app.wallet
  (:require [reagent.core :as reagent :refer [atom]]
            [app.storage :refer [state accounts coins local]]
            [goog.object :as g]
            [async-await.core :refer [async await]]))

(defn wallet [] (g/get js/window "wallet"))

(defn init-wallet []
  (swap! state assoc :accounts (.getAccounts (.-masterAccount (wallet))))
  (doseq [acc (.getAccounts (.-masterAccount (wallet)))]
    (swap! accounts conj {:name (.-name acc)
                          :keys {:incognito (.-paymentAddressKeySerialized (.-keySet (.-key acc)))
                                 :public (.-publicKeySerialized (.-keySet (.-key acc)))
                                 :private (.-privateKeySerialized (.-keySet (.-key acc)))
                                 :readonly (.-viewingKeySerialized (.-keySet (.-key acc)))
                                 :validator (.-validatorKey (.-keySet (.-key acc)))}
                          :coins [{:id 0
                                   :amount 0}
                                  {:id 1
                                   :amount 1}
                                  {:id 2
                                   :amount 2}
                                  {:id 3
                                   :amount 40}
                                  {:id 4
                                   :amount 0}]})))
    ;(js/console.log (.-privacyTokenIds acc))))

(defn create-backup []
  (swap! local assoc :backupkey (.backup (wallet) (-> (wallet) .-mnemonic)))
  (swap! state assoc :render (.getTime (js/Date.))))
;  (init-wallet))