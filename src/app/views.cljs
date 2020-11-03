(ns app.views
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [app.storage :refer [state accounts coins local]]
            [app.wallet :refer [wallet init-wallet]]
            [app.accounts :refer [accounts-container reciepent-address? switch-reciepent-account]]
            [app.header :refer [header]]
            [app.coins :refer [coins-container]]
            [app.actions :refer [actions-container]]
            [app.icons :refer [loader bulb-icon info-icon]]
            [goog.string :as gstring :refer [format]]
            [goog.string.format]
            ["@tippyjs/react" :default Tippy :refer (useSingleton)]
            ["tippy.js" :refer (animateFill)]
            [app.pin :refer [login-screen]]))


(defn theme-switcher [theme desc]
  [:button.theme-selector
    {:class (when (= theme (@local :theme)) "theme-selector--active")
     :on-click #(do
                    (set! (.. js/document -body -className) theme)
                    (swap! local assoc :theme theme))}
    [:img {:src (str "/images/themes/" theme ".png") :width "180px"}]
    [:p desc]])

(defn load-theme []
  (when (not (:theme @local))
    (swap! local assoc :theme "auto")))

(defn themes []
  [:div#themes.tooltip--padding
    [theme-switcher "light" "Light"]
    [theme-switcher "dark" "Dark"]
    [theme-switcher "auto" "Browser setting"]])

(defn about []
  [:div#about.tooltip--padding
    [:img {:src "/images/zgen-logo.svg" :width "80px"}]
    [:div
      [:p "Made by " [:a {:href "https://zgen.hu" :target "_blank"} "ZGEN DAO"] ", the bureaucracy-free online guild."]
      [:p "Send your feature requests to: " [:a {:href "mailto:contact@zgen.hu" :target "_blank"} "crypto@zgen.hu"]]
      [:p "Source: " [:a {:href "https://github.com/zgendao/incognito-web-wallet" :target "_blank"} "zgendao/incognito-web-wallet"]]]])

(defn navbar-tooltip [title content icon instance-to-hide target]
  (let [singleton-instance (@state :navbar-tippy-instance)]
    [:> Tippy {:content title :singleton target}
      [:> Tippy {:content (reagent/as-element [content]) :allowHTML true :interactive true :interactiveBorder 50
                 :maxWidth 700 :trigger "click" :animateFill true :plugins #js [animateFill]
                 :onCreate (fn [instance] (swap! state assoc (str "navbar-tippy-" title) instance))
                 :onShow (fn [instance] (.setProps instance #js {:trigger "mouseenter"})
                                        (.setProps singleton-instance #js {:trigger "click"}))
                 :onHide (fn [instance] (.setProps instance #js {:trigger "click"})
                                        (.hide singleton-instance)
                                        (.setProps singleton-instance #js {:trigger "mouseenter"}))}
        [:button.display-icon {:onMouseEnter (fn [] (.hide (@state instance-to-hide)))}
          [icon]]]]))

(defn navbar-content [key]
  (let [[source target] (useSingleton)]
    (reagent/as-element
      [:div.navbar__content
        [:> Tippy {:singleton source
                   :hideOnClick false
                   :interactive true
                   :zIndex 0
                   :animation "shift-away"
                   :moveTransition "transform 0.4s cubic-bezier(0.22, 1, 0.36, 1) 0s"
                   :onCreate (fn [instance] (swap! state assoc :navbar-tippy-instance instance))}]
        [navbar-tooltip "Theme" themes bulb-icon "navbar-tippy-About" target]
        [navbar-tooltip "About" about info-icon "navbar-tippy-Theme" target]])))

(defn navbar [show-content?]
  [:nav
   [:div.container
    [:div.navbar__brand
      [:img {:src "./images/logo.png" :width "30px" :height "30px"}]
      [:p "Incognito Web Wallet"]]
    (when show-content?
      [:> navbar-content])]])

(defn main []
      (create-class
        {:component-did-mount
          (fn []
            (init-wallet))
         :reagent-render
          (fn []
            (js/setInterval #(init-wallet) 60000)
            (js/console.log (wallet))
            [:div#main
              [:div.container
               [header]
               [coins-container]
               [actions-container]]])}))

(defn back-layer []
  [:div#backLayer.clickCatcher
    {:class [(when (or (= (@state :selected-coin) "?")
                       (reciepent-address? "?")) "active")]
     :on-click (cond (= (@state :selected-coin) "?") #(swap! state assoc :selected-coin nil)
                     (reciepent-address? "?") #(switch-reciepent-account nil))}])

(defn download-from [store link]
  [:a {:href link}
    [:img {:src (str "./images/appStoreLogos/" store ".png")}]])

(defn mobile-view []
  [:<>
    [navbar false]
    [:div.container.mobileView
      [:h1 "Looks like you're on mobile. Use the app instead!"]
      [:p "Web Wallet was designed for tablets, laptops and desktops,
          and currently isn't available on mobile devices."]
      [:div
        [download-from "appstore" "https://apps.apple.com/us/app/incognito-crypto-wallet/id1475631606?ls=1"]
        [download-from "googleplay" "https://play.google.com/store/apps/details?id=com.incognito.wallet"]
        [download-from "directapk" "https://github.com/incognitochain/incognito-wallet/releases/download/v3.7.2/3.7.2.apk"]]]])

(defn app []
  (create-class
    {:component-did-mount
     (do (load-theme)
         #(set! (.. js/document -body -className) (@local :theme)))
     :reagent-render
     (fn []
       (if (>= js/window.screen.width 1024)
         (if (and (:pw @state) (:wasm-loaded @state))
           [:<>
             [navbar true]
             [accounts-container]
             [main]
             [back-layer]]
           [:<>
             [navbar false]
             [login-screen]])
         [mobile-view]))}))
