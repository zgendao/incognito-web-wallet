(ns app.views
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [app.storage :refer [state accounts coins]]
            [app.api :refer [wallet]]
            [app.accounts :refer [accounts-container reciepent-address? switch-reciepent-account]]
            [app.header :refer [header]]
            [app.coins :refer [coins-container]]
            [app.actions :refer [actions-container]]
            [app.icons :refer [loader]]
            [goog.string :as gstring :refer [format]]
            [goog.string.format]
            ["incognito-js" :as incognito-js]
            [goog.object :as g]))

(defn navbar [showExchangeRate?]
  [:nav
   [:div.container
    [:div.navbar__brand
      [:img {:src "./public/images/logo.png" :width "30px" :height "30px"}]
      [:p "Incognito Web Wallet"]]
    (when showExchangeRate?
      [:div
        [:p (format "%.2f" (:prv-price @state)) " USD"]])]])

(defn about []
  [:div#about.container
    [:p "Made by " [:a {:href "https://zgen.hu" :target "_blank"} "ZGEN DAO"] ", the bureaucracy-free online guild."]
    [:p "Send your feature requests to: " [:a {:href "mailto:contact@zgen.hu" :target "_blank"} "crypto@zgen.hu"]]
    [:p "Source: " [:a {:href "https://github.com/zgendao/incognito-web-wallet" :target "_blank"} "zgendao/incognito-web-wallet"]]])

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
                     (reciepent-address? "?") #(switch-reciepent-account nil))}])

(defn download-from [store link]
  [:a {:href link}
    [:img {:src (str "./public/images/appStoreLogos/" store ".png")}]])
    
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
        (when (@state :selected-account) [about])
        [back-layer]]
      [:<>
        [navbar]
        [loader]])
    [mobile-view]))
