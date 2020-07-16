(ns app.views
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            ["qrcode" :as qrcode]
            [app.state :refer [state]]
            [app.api :refer [wallet]]))

(defn qr-code [text]
  (create-class
   {:component-did-mount
    (fn [element]
      (qrcode/toCanvas
       (dom-node element)
       text))
    :reagent-render (fn []
                      [:canvas#canvas])}))

(defn wallet-ui []
  (create-class
   {:component-did-mount
    (fn []
      (.then
       (.init wallet "my-passphrase" "TEST-WALLET")
       #(swap! state assoc :accounts (.getAccounts (.-masterAccount wallet)))))
    :reagent-render
    (fn []
      (let [account (first (js->clj (:accounts @state)))]
        [:div {:style {:display "flex" :justify-content "center" :align-items "center" :flex-direction "column" :padding-bottom "60px" :padding-top "30px"}}
         [:h3 "Your Web Wallet:"]
         (when account
           [:div
            [:h6 "Name: " (-> account .-name)]
            [qr-code
             (-> account .-key .-keySet .-paymentAddressKeySerialized)]
            [:div
             [:label {:for "payment"} "Payment key:"]
             [:input {:type "text" :id "payment" :value (-> account .-key .-keySet .-paymentAddressKeySerialized)}]]
            [:div
             [:label {:for "private"} "Private key:"]
             [:input {:type "text" :id "private" :value (-> account .-key .-keySet .-privateKeySerialized)}]]
            [:div
             [:label {:for "validator"} "Validator key:"]
             [:input {:type "text" :id "validator" :value (-> account .-nativeToken .-accountKeySet .-validatorKey)}]]])]))}))

(defn app []
  [:div {:style {:display "flex" :justify-content "center" :align-items "center" :flex-direction "column"}}
   [:h1 (:prv-price @state)]
   (if (:wasm-loaded @state)
     [wallet-ui]
     [:h1 "Loading.."])])
