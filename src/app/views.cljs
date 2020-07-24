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
            [account-selector (str "Account " x) "300"])])}))

(defn accounts []
  [:div#accounts.container {:class [(when-not (= (@state :selected-account) 0) "collapsed")]}
    [:div.accounts-wrapper
      [accounts-grid]]
    [:button.circle-btn {:on-click #(swap! state assoc :selected-account 0)} "v"]])

(defn key-elem [name address]
  [:div.key-elem
    [:p name]
    [:div
      [:a {:href "asd.com"} address]
      [:span "i"]]])

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
                  "v"]]])))}))
(defn app []
  (if (:wasm-loaded @state)
    [:<>
      [navbar]
      [accounts]
      [main]]
    [:h1 "Loading.."]))
