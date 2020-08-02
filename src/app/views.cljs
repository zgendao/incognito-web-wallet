(ns app.views
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            ["qrcode" :as qrcode]
            [app.storage :refer [state accounts coins]]
            [app.api :refer [wallet]]
            [app.accounts :refer [accounts-container reciepent-address?]]
            [app.header :refer [header]]
            [app.coins :refer [coins-container]]
            [app.actions :refer [actions-container]]
            [goog.string :as gstring :refer [format]]
            [goog.string.format]))

(defn qr-code [text]
  (create-class
   {:component-did-mount
    (fn [element]
      (qrcode/toCanvas
       (dom-node element)
       text))
    :reagent-render (fn [] [:canvas#canvas])}))

(defn navbar []
  [:nav
   [:div.container
    [:div.navbar__brand
      [:img {:src "./images/logo.png" :width "30px" :height "30px"}]
      [:p "Incognito Web Wallet"]]
    [:div
      [:p (format "%.2f" (:prv-price @state)) " USD"]]]])

(defn main []
  (create-class
    {:component-did-mount
      (fn []
        (.then
          (.init wallet "my-passphrase" "TEST-WALLET")
          #(swap! state assoc :accounts (.getAccounts (.-masterAccount wallet)))))
     :reagent-render
      (fn []
        (let [account (first (js->clj (:accounts @state)))]
          (when account
            [:div#main.container {:class [(when-not (@state :selected-account) "hidden")]}
              [header]
              [coins-container]
              [actions-container]])))}))

(defn back-layer []
  [:div#backLayer.clickCatcher
    {:class [(when (or (= (@state :selected-coin) "?")
                       (reciepent-address? "?")) "active")]
     :on-click (cond (= (@state :selected-coin) "?") #(swap! state assoc :selected-coin nil)
                     (reciepent-address? "?") #(swap! state assoc-in [:send-data :reciepent-address] nil))}])
                
(defn app []
  (if (:wasm-loaded @state)
    [:<>
      [navbar]
      [accounts-container]
      [main]
      [back-layer]]
    [:h1 "Loading.."]))
