(ns app.views
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            ["qrcode" :as qrcode]
            [app.state :refer [state]]
            [app.api :refer [wallet]]
            [goog.string :as gstring :refer [format]]
            [goog.string.format]
            [app.accounts :refer [accounts]]
            [app.tabs :refer [tabs-component]]))

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

(defn key-elem [name address]
  [:div.key-elem
    [:p name]
    [:div
      [:a address]
      [:span "i"]]])

(defn coin [name symbol amount]
  [:div.coin-wrapper {:class [(when (= (@state :selected-coin) symbol) "selected")]}
    [:div.coin {:on-click #(swap! state assoc :selected-coin symbol)}
      [:div.coin__img
        [:img {:src (str "./images/coinLogos/" symbol ".png")}]]
      [:div.coin__content
        [:div.coin__content__main
          [:h6 name]
          [:p amount]]
        [:div.coin__content__prv
          [:p "$295.6654"]
          [:p "$" (* amount 0.91)]]]]
    [:button.circle-btn {:on-click #(swap! state assoc :selected-coin false)} ">"]])

(defn input [name label type placeholder end-element click-catcher]
  [:div.input-group
    [:label {:for name} label]
    [:div.input-wrapper
      [:input {:name name :type type :placeholder placeholder}]
      (when end-element
        [:span
          end-element])
      (when click-catcher
        click-catcher)]])

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
            [:div#main.container {:class [(when (= (@state :selected-account) 0) "hidden")]}
              [:div.header__amount
                ;[:h6 "Name: " (-> account .-name)]
                [:div
                  [:p "Total shielded balance"]
                  [:h1 "$ 101.3245"]]
                [:button.btn "+ Shield crypto"]]
              [:div.header__keys
                ;[qr-code
                ;  (-> account .-key .-keySet .-paymentAddressKeySerialized)
                [:div.keys-wrapper
                  ;the modal below is absolute positioned, so a dummy element is needed for correct spacing
                  ;[:p "-"]
                  [:div.keys-modal {:class [(when (@state :keys-opened) "opened")]}
                    [key-elem "Incognito address:"
                              (-> account .-key .-keySet .-paymentAddressKeySerialized)]
                    [:div
                      [key-elem "Payment key:"
                                (-> account .-key .-keySet .-paymentAddressKeySerialized)]
                      [key-elem "Private key:"
                                (-> account .-key .-keySet .-privateKeySerialized)]
                      [key-elem "Validator key:"
                                (-> account .-nativeToken .-accountKeySet .-validatorKey)]]]]
                [:button.circle-btn {:on-click #(swap! state assoc :keys-opened (not (@state :keys-opened)))}
                  "v"]]
              [:div.coins-container {:class [(when (= (@state :selected-coin) "?") "highlighted")]}
                [coin "Privacy" "PRV" 73.44]
                [coin "Ethereum" "ETH" 0.000456]
                [coin "Bitcoin" "BTC" 0.0000193]
                [coin "Tether USD" "USDT" 40]
                [coin "Dai Stablecoin" "DAI" 0]]
              [:div.actions-container
                [:div.actions-wrapper
                  [tabs-component
                    {"Transaction history"
                      [:p "Transaction history"]
                     "Send"
                      [:form.send-wrapper
                        [input "reciepent" "To" "text" "Enter address (Incognito or external)"
                          [:button {:type "button" :on-click #(swap! state assoc :reciepent-address "?")} "i"]]
                        [input "amount" "Amount" "number"
                          (if (@state :selected-coin) "0" "Select coin first")
                          (when (@state :selected-coin) (@state :selected-coin))
                          (when-not (@state :selected-coin)
                            [:div.clickCatcher.active {:on-click #(swap! state assoc :selected-coin "?")}])]
                        [input "fee" "Fee" "number" "0.00" "PRV"]
                        [input "memo" "Memo" "text" "Add note (optional)"]
                        [:div.btn-wrapper
                          [:button.btn.btn--primary "Send Anonymously"]]]}]]]])))}))
                
(defn app []
  (if (:wasm-loaded @state)
    [:<>
      [navbar]
      [accounts]
      [main]
      [:div#backLayer.clickCatcher
        {:class [(when (or (= (@state :selected-coin) "?")
                           (= (@state :reciepent-address) "?"))
                    "active")]
         :on-click (cond (= (@state :selected-coin) "?") #(swap! state assoc :selected-coin false)
                         (= (@state :reciepent-address) "?") #(swap! state assoc :reciepent-address false))}]]
    [:h1 "Loading.."]))
