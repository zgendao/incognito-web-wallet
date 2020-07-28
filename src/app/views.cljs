(ns app.views
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [cljs.core.async :refer [<!]]
            ["qrcode" :as qrcode]
            [app.state :refer [state local]]
            ["incognito-js" :as incognito-js]))

(defn generate-wallet [wallet name]
  (.then
   (.init wallet "xyz" name)
   #(swap! local assoc :backupkey (.backup % (-> % .-mnemonic)) :pw (-> % .-mnemonic))))

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

(defn wallet-core [wallet]
  [:div {:style {:display "flex" :justify-content "center" :align-items "center" :flex-direction "column" :padding-bottom "60px" :padding-top "30px"}}
   [:h3 "Your Web Wallet:"
    [:div
     (js/console.log wallet)
     [:h6 "Name: " (-> wallet .-name)]
     [qr-code (-> wallet .-masterAccount .-key .-keySet .-paymentAddressKeySerialized)]
     [:div
      [:label {:for "payment"} "Payment key:"]
      [:input {:type "text" :id "payment" :value (-> wallet .-masterAccount .-key .-keySet .-paymentAddressKeySerialized)}]]
     [:div
      [:label {:for "private"} "Private key:"]
      [:input {:type "text" :id "private" :value (-> wallet .-masterAccount .-key .-keySet .-privateKeySerialized)}]]
     [:div
      [:label {:for "validator"} "Validator key:"]
      [:input {:type "text" :id "validator" :value (-> wallet .-masterAccount .-key .-keySet .-viewingKeySerialized)}]]]
    [:button {:on-click #(reset! local {})} "Delete Wallet"]]])

(defn wallet-ui [wallet]
  (create-class
   {:reagent-render
    (fn []
      (wallet-core wallet))}))

(def wname (atom nil))
(defn create-wallet [wallet]
  [:div {:style {:display "flex" :justify-content "center" :align-items "center" :flex-direction "column"}}
   [:p "Your wallet name:"
    [:input {:type "text"
             :value @wname
             :on-change #(reset! wname (-> % .-target .-value))}]
    [:button {:on-click #(generate-wallet wallet @wname)} "Create"]]])

(defn app [wallet]
  (js/console.log wallet)
  [:div {:style {:display "flex" :justify-content "center" :align-items "center" :flex-direction "column"}}
   [:h3 (:prv-price @state)]
   (if (:wasm-loaded @state)
     [:div {:style {:width "100%"}}
      (if (:backupkey @local)
        [wallet-ui wallet]
        [create-wallet wallet])]
     [:h2 "Loading.."])])
