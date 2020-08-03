(ns app.views
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            ["qrcode" :as qrcode]
            [app.state :refer [state local]]
            ["incognito-js" :as incognito-js]
            [async-await.core :refer [async await]]))

(defn qr-code [text]
  (create-class
   {:component-did-mount
    (fn [element]
      (qrcode/toCanvas
       (dom-node element)
       text))
    :reagent-render
    (fn []
      [:canvas#canvas])}))

(defn create-backup [wallet]
  (swap! local assoc :backupkey (.backup wallet (-> wallet .-mnemonic)))
  (swap! state assoc :render (.getTime (js/Date.))))

(defn get-balance [account]
  (.then
   (-> account .-nativeToken (.getAvaiableBalance))
   #(swap! state assoc-in [:balances (-> account .-name)] (/ % 1000000000))))

(defn follow-token [wallet account token-id]
  (-> account (.followTokenById token-id))
  (create-backup wallet))

(defn unfollow-token [wallet account token-id]
  (-> account (.unfollowTokenById token-id))
  (create-backup wallet))

(defn get-following-tokens [token-ids]
  (filter (fn [token] (some #(= % (:TokenID token)) token-ids)) (@state :ptokens)))

(defn get-ptoken [account token-id symbol]
  (async
   (let [token (await (-> account (.getFollowingPrivacyToken token-id)))
         balance (await (-> token (.getAvaiableBalance)))]
     (when (> balance 0)
       (swap! state assoc-in [:ptokens-balance token-id] {:symbol symbol :balance (first (-> balance .-words))})))))

(defn get-ptokens-balance [account]
  (for [token (@state :ptokens)]
    (do (println token) ;NEM FUT EZ A CIKLUS WHY
        (get-ptoken account (:TokenID token) (:Symbol token)))))

(defn get-history [account] ;NEMJÓMMÉG
  (.then
   (incognito-js/historyServices.getTxHistoryByPublicKey (-> account .-key .-keySet .-publicKeySerialized) nil)
   #(js/console.log %)))

(defn add-account [wallet name]
  (async
   (await (-> wallet .-masterAccount (.addAccount name)))
   (create-backup wallet)))

(defn remove-account [wallet name]
  (-> wallet .-masterAccount (.removeAccount name))
  (create-backup wallet))

(defn import-account [wallet name privkey]
  (-> wallet .-masterAccount (.importAccount name privkey))
  (create-backup wallet))

(defn get-accounts [wallet]
  (-> wallet .-masterAccount .getAccounts))

(defn send-prv [account amount to]
  (.then
   (-> account .-nativeToken (.transfer (clj->js [{:paymentAddressStr to :amount amount :message ""}]) 10))
   (fn [his] (js/console.log his))))

(defn account [wallet acc]
  (reagent/create-class
   {:component-did-mount (fn [] (do (get-balance acc) (get-ptokens-balance acc)))
    :reagent-render
    (fn []
      (let [payment (-> acc .-key .-keySet .-paymentAddressKeySerialized)
            private (-> acc .-key .-keySet .-paymentAddressKeySerialized)
            validator (-> acc .-key .-keySet .-viewingKeySerialized)
            name (-> acc .-name)
            send-amount-key (keyword (str name "-send-amount"))
            send-to-key (keyword (str name "-send-to"))
            token-id-key (keyword (str name "-token-id"))
            _ (get-ptokens-balance acc)]
        [:div {:style {:margin-bottom "40px"}}
         [:h4 "Account name: " name]
         [:p (str "Balance " (get-in @state [:balances name]) " PRV")]
         [qr-code payment]
         [:div
          [:label {:for "payment"} "Payment key:"]
          [:input {:type "text" :id "payment" :value payment}]]
         [:div
          [:label {:for "private"} "Private key:"]
          [:input {:type "text" :id "private" :value private}]]
         [:div
          [:label {:for "validator"} "Validator key:"]
          [:input {:type "text" :id "validator" :value validator}]]
         [:div
          [:p "Followed pTokens"]
          [:input {:type "text"
                   :value (@state token-id-key)
                   :placeholder "token id"
                   :on-change #(swap! state assoc token-id-key (-> % .-target .-value))}]
          [:button {:on-click #(follow-token wallet acc (@state token-id-key))} "Follow"]
          [:button {:on-click #(unfollow-token wallet acc (@state token-id-key))} "Unfollow"]
          (for [ptoken (get-following-tokens (-> acc .-privacyTokenIds))]
            [:div
             [:h6 (str (:PSymbol ptoken) ": ")
              [:input {:type "text" :value (:TokenID ptoken)}]]])]
         [:div
          [:p "pTokens balance"]
          (for [[k v] (@state :ptokens-balance)]
            [:div
             [:h6 (str (:symbol v) ": " (:balance v))]])]
         [:div
          [:p "Send PRV"]
          [:input {:type "number"
                   :value (@state send-amount-key)
                   :placeholder "amount"
                   :on-change #(swap! state assoc send-amount-key (js/parseInt (-> % .-target .-value)))}]
          [:input {:type "text"
                   :value (@state send-to-key)
                   :placeholder "address"
                   :on-change #(swap! state assoc send-to-key (-> % .-target .-value))}]
          [:button {:on-click #(send-prv acc (@state send-amount-key) (@state send-to-key))} "Send"]]
         [:button {:style {:margin-top "20px"} :on-click #(remove-account wallet (-> acc .-name))} "Remove this acc"]]))}))

(defn wallet-ui [wallet]
  (reagent/create-class
   {:reagent-render
    (fn []
      [:div {:style {:display "flex" :justify-content "center" :align-items "center" :flex-direction "column" :padding-bottom "60px" :padding-top "30px"}}
       [:div
        [:p "Create Account"]
        [:input {:type "text"
                 :value (@state :acc-name)
                 :placeholder "name"
                 :on-change #(swap! state assoc :acc-name (-> % .-target .-value))}]
        [:button {:on-click #(add-account wallet (@state :acc-name))} "Create"]
        [:button {:on-click #(get-accounts wallet)} "Get Account"]
        [:button {:on-click #(reset! local {})} "Delete Wallet"]]
       [:div
        [:p "Import Account"]
        [:input {:type "text"
                 :value (@state :imp-name)
                 :placeholder "name"
                 :on-change #(swap! state assoc :imp-name (-> % .-target .-value))}]
        [:input {:type "text"
                 :value (@state :priv-key)
                 :placeholder "private key"
                 :on-change #(swap! state assoc :priv-key (-> % .-target .-value))}]
        [:button {:on-click #(import-account wallet (@state :imp-name) (@state :priv-key))} "Import"]]
       [:h3 (str (-> wallet .-name) " Wallet:")
        (for [acc (get-accounts wallet)]
          [account wallet acc])]])}))

(defn generate-wallet [wallet name]
  (.then
   (.init wallet (str (.getTime (js/Date.))) name)
   #(swap! local assoc :backupkey (.backup % (-> % .-mnemonic)) :pw (-> % .-mnemonic))))

(defn create-wallet [wallet]
  [:div {:style {:display "flex" :justify-content "center" :align-items "center" :flex-direction "column"}}
   [:p "Your wallet name:"
    [:input {:type "text"
             :value (@state :wall-name)
             :on-change #(swap! state assoc :wall-name (-> % .-target .-value))}]
    [:button {:on-click #(generate-wallet wallet (@state :wall-name))} "Create"]]])

(defn app [wallet]
  [:div {:style {:display "flex" :justify-content "center" :align-items "center" :flex-direction "column"}}
   [:h4 (str "PRV price: $" (:prv-price @state))]
   (if (:wasm-loaded @state)
     [:div {:style {:width "100%"}}
      (if (:backupkey @local)
        [wallet-ui wallet]
        [create-wallet wallet])]
     [:h2 "Loading.."])])
