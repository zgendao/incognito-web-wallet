(ns app.views
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [app.storage :refer [state accounts coins local]]
;            [app.api :refer [wallet init-wallet]]
            [app.accounts :refer [accounts-container reciepent-address? wallet init-wallet]]
            [app.header :refer [header]]
            [app.coins :refer [coins-container]]
            [app.actions :refer [actions-container]]
            [goog.string :as gstring :refer [format]]
            [goog.string.format]
            ["incognito-js" :as incognito-js]
            [goog.object :as g]))

(defn navbar [showExchangeRate?]
  [:nav
   [:div.container
    [:div.navbar__brand
      [:img {:src "./images/logo.png" :width "30px" :height "30px"}]
      [:p "Incognito Web Wallet"]]
    (when showExchangeRate?
      [:div
        [:p (format "%.2f" (:prv-price @state)) " USD"]])]])

(defn main []
  (create-class
    {:component-did-mount
      (fn []
        (init-wallet)
        (js/console.log (wallet)))
;        (js/console.log (.-privateKeySerialized (.-keySet (.-key (.pop (.slice (.getAccounts (.-masterAccount (wallet))) -1)))))))
        ;  (js/console.log (.-name acc))))
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

(defn download-from [store link]
  [:a {:href link}
    [:img {:src (str "./images/appStoreLogos/" store ".png")}]])

(defn mobile-view []
  [:<>
    [navbar]
    [:div.container.mobileView
      [:h1 "Looks like you're on mobile. Use the app instead!"]
      [:p "Web Wallet was designed for tablets, laptops and desktops,
          and currently isn't available on mobile devices."]
      [:div
        [download-from "appstore" "https://apps.apple.com/us/app/incognito-crypto-wallet/id1475631606?ls=1"]
        [download-from "googleplay" "https://play.google.com/store/apps/details?id=com.incognito.wallet"]
        [download-from "directapk" "https://github.com/incognitochain/incognito-wallet/releases/download/v3.7.2/3.7.2.apk"]]]])

(defn app []
  (if (>= js/window.screen.width 1024)
    (if (:wasm-loaded @state)
      [:<>
        [navbar true]
        [accounts-container]
        [main]
        [back-layer]]
      [:h1 "Loading.."])
    [mobile-view]))
