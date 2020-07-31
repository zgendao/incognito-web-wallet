(ns app.views
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [cljs.core.async :refer [<!]]
            ["qrcode" :as qrcode]
            [app.state :refer [state local]]
            ["incognito-js" :as incognito-js]
            [async-await.core :refer [async await]]))

(def render (atom 0))
(def balances (atom {}))

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
  (reset! render (inc @render)))

(defn get-balance [account]
  (.then
   (-> account .-nativeToken (.getTotalBalance))
   #(swap! balances assoc (-> account .-name) (/ % 1000000000))))

(defn get-history [account]
  (async
   (await (-> account .-nativeToken (.getTxHistories)))))

(defn add-account [wallet name]
  (async
   (await (-> wallet .-masterAccount (.addAccount name)))
   (create-backup wallet)))

(defn remove-account [wallet name]
  (-> wallet .-masterAccount (.removeAccount name))
  (create-backup wallet))

(defn import-account [wallet name privkey]
  (-> wallet .-masterAccount (.importAccount name privkey))
  (.setTimeout (create-backup wallet) 5000))

(defn get-accounts [wallet]
  (-> wallet .-masterAccount .getAccounts))

(defn send-prv [account amount to]
  (.then
   (-> account .-nativeToken (.transfer (clj->js [{:paymentAddressStr to :amount amount :message ""}]) 10))
   #(js/console.log %)))

(defn account [wallet acc]
  (reagent/create-class
   {:component-did-mount (fn [] (do (get-balance acc)))
    :reagent-render
    (fn []
      [:div {:style {:margin-bottom "40px"}}
       [:h4 "Account name: " (-> acc .-name)]
       [:p (str "Balance " (@balances (-> acc .-name)))]
       [qr-code (-> acc .-key .-keySet .-paymentAddressKeySerialized)]
       [:div
        [:label {:for "payment"} "Payment key:"]
        [:input {:type "text" :id "payment" :value (-> acc .-key .-keySet .-paymentAddressKeySerialized)}]]
       [:div
        [:label {:for "private"} "Private key:"]
        [:input {:type "text" :id "private" :value (-> acc .-key .-keySet .-privateKeySerialized)}]]
       [:div
        [:label {:for "validator"} "Validator key:"]
        [:input {:type "text" :id "validator" :value (-> acc .-key .-keySet .-viewingKeySerialized)}]]
       [:div
        [:p "Send PRV"]
        [:input {:type "number"
                 :value (@state :send-amount)
                 :placeholder "amount"
                 :on-change #(swap! state assoc :send-amount (js/parseInt (-> % .-target .-value)))}]
        [:input {:type "text"
                 :value (@state :send-to)
                 :placeholder "address"
                 :on-change #(swap! state assoc :send-to (-> % .-target .-value))}]
        [:button {:on-click #(send-prv acc (@state :send-amount) (@state :send-to))} "Send"]]
       [:button {:style {:margin-top "20px"} :on-click #(remove-account wallet (-> acc .-name))} "Remove this acc"]])}))

(defn wallet-ui [wallet]
  (fn []
    @render
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
               :value (@state :priv-name)
               :placeholder "private key"
               :on-change #(swap! state assoc :priv-key (-> % .-target .-value))}]
      [:button {:on-click #(import-account wallet (@state :imp-name) (@state :priv-name))} "Import"]]
     [:h3 (str (-> wallet .-name) " Wallet:")
      (for [acc (get-accounts wallet)]
        [account wallet acc])]]))

(defn render-wallet [wallet]
  (reagent/create-class
   {:reagent-render (fn [] [wallet-ui wallet])}))

(defn generate-wallet [wallet name]
  (.then
   (.init wallet (.getTime (js/Date.)) name)
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
   [:h3 (:prv-price @state)]
   (if (:wasm-loaded @state)
     [:div {:style {:width "100%"}}
      (if (:backupkey @local)
        [wallet-ui wallet]
        [create-wallet wallet])]
     [:h2 "Loading.."])])
