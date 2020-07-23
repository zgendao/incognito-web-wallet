(ns app.views
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            ["qrcode" :as qrcode]
            [app.state :refer [state]]
            [app.api :refer [wallet]]
            [animate-css-grid :refer [wrapGrid]]))

(defn qr-code [text]
  (create-class
   {:component-did-mount
    (fn [element]
      (qrcode/toCanvas
       (dom-node element)
       text))
    :reagent-render (fn []
                      [:canvas#canvas])}))

(defn navbar []
  [:nav
   [:div.container
    [:div.navbar__brand
      [:img {:src "./images/logo.png" :width "30px" :height "30px"}]
      [:p "Incognito Web Wallet"]]
    [:div
      [:p (:prv-price @state) " USD"]]]])

(defn account-selector [name balance]
  [:div
    [:div.account-selector {:on-click #(swap! state assoc :selected-account name)
                            :class [(when (= (@state :selected-account) name) "account-selector--active")]}
      [:div
        [:h6.account-selector__name name]
        [:div.account-selector__balance
          [:p balance]]]]])

(defn accounts-grid []
  (create-class
    {:component-did-mount
      (fn []
        (do
          (def grid (.querySelector js/document ".accounts-grid"))
          (wrapGrid grid #js {:duration 500})))
     :reagent-render
      (fn []
        [:div.accounts-grid
          (for [x (range 0 10)]
            [account-selector (str "Wallet " x) "300"])])}))

(defn accounts []
  [:div#accounts.container {:class [(when-not (= (@state :selected-account) 0) "collapsed")]}
    [:div.accounts-wrapper
      [accounts-grid]]
    [:button.circle-button {:on-click #(swap! state assoc :selected-account 0)} "v"]])

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
        [:div.container
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
  (if (:wasm-loaded @state)
    [:<>
      [navbar]
      [accounts]
      [wallet-ui]]
    [:h1 "Loading.."]))
