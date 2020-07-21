(ns app.views
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [cljs.core.async :refer [<!]]
            ["qrcode" :as qrcode]
            [app.state :refer [state local]]
            [app.api :refer [wallet wallett MasterAccount]]))

(defn qr-code [text]
  (create-class
   {:component-did-mount
    (fn [element]
      (qrcode/toCanvas
       (dom-node element)
       text))
    :reagent-render (fn []
                      [:canvas#canvas])}))

(defn wallet-core [account]
  (let []
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
                     [:input {:type "text" :id "validator" :value (-> account .-nativeToken .-accountKeySet .-validatorKey)}]]
                    [:div
                      [:button {:on-click #(swap! local assoc :wallets (js->clj (:masteraccounts @state)))} "Save"]]
                               ])]))

(defn wallet-ui []
  (create-class
   {:component-did-mount
    (fn []
      (.then
       (.init wallet "my-passphrase" "TEST-WALLET")
       #(do (swap! state assoc :accounts (.getAccounts (.-masterAccount wallet)))
       (swap! state assoc :masteraccounts (.backup wallet "abc"))
       (swap! state assoc :wall wallet))
         ))
    :reagent-render
    (fn []
      (wallet-core (first (js->clj (:accounts @state)))))
        }))

;EZ AZ IMPORTÁLÓS, NEM MŰKÖDIK
(defn stored-wallet-ui []
  (create-class
   {:component-did-mount
    (fn []
      (.then (.importAccount (.-masterAccount wallet) (first (:wallets @local)) (second (:wallets @local)))
        #(swap! state assoc :importaccounts %))
         )
    :reagent-render
    (fn []
      (wallet-core (:importaccounts @state)))
                }))

(defn app []
  (let [
        accvector (:wallets @local)
        ]
  [:div {:style {:display "flex" :justify-content "center" :align-items "center" :flex-direction "column"}}
   [:h1 (:prv-price @state)]
   (if (:wasm-loaded @state)
     [:div {:style {:width "100%"}}
     [wallet-ui]
     [:p (str accvector)]
     [:p (str (type (:masteraccounts @state)))]
     [:div
       [:button {:on-click #(.then (.restore wallet accvector "abc")
         (swap! state assoc :importaccounts "hello"))} "Mutat"]]
    ;BACKUPDATA:
     [:p (str (type (js->clj accvector)))]
     [:p local]
     [:p state]
    ;EREDETI ACCOUNT:
     [:p (str (type (first (js->clj (:accounts @state)))))]
     ]
     [:h1 "Loading.."])]))
