(ns app.coins
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [app.storage :refer [state accounts coins local]]
            [app.icons :refer [arrow-right-icon plus-icon]]
            [goog.string :as gstring :refer [format]]
            [goog.string.format]
            ["@tippyjs/react" :default Tippy]))

(defn switch-coin [to]
  (swap! state assoc :selected-coin to)
  (swap! state assoc-in [:send-data :amount] nil)
  (swap! state assoc-in [:send-data :in-confirm-state] false))

(defn coin [id amount]
  (let [name (get-in @local [:coins id :Name])
        symbol (get-in @local [:coins id :Symbol])
        priceInUSD (get-in @local [:coins id :PriceUsd])]
    [:div.coin-wrapper {:class [(when (= (@state :selected-coin) id) "selected")]}
      [:> Tippy {:content "Select to send" :animation "shift-away" :delay #js [500 0]}
        [:div.coin {:on-click #(switch-coin id)}
          [:div.coin__img
            [:img {:src (str "./node_modules/cryptocurrency-icons/svg/color/" (clojure.string/lower-case symbol) ".svg") :onError #(set! (-> % .-target .-src)  "./node_modules/cryptocurrency-icons/svg/color/notfound.svg")}]]
          [:div.coin__content
            [:div.coin__content__main
              [:h6 name]
              [:p amount]]
            [:div.coin__content__prv
              [:p "$" (format "%.2f" priceInUSD)]
              [:p "$" (format "%.2f" (* amount priceInUSD))]]]]]
      [:button.circle-btn {:on-click #(switch-coin nil)}
        [arrow-right-icon]]]))

(defn coins-container []
  (into [:div.coins-container {:class [(when (= (@state :selected-coin) "?") "highlighted")]}]
    [(map (fn [{:keys [id amount]}]
            ^{:key id} [coin id amount])
        (get-in @local [:accounts (@local :selected-account) :coins] []))
     [:> Tippy {:content "Not available yet" :interactive true :placement "top-start" :animation "shift-away"}
      [:div.coin-wrapper.disabled {:style {:width "auto" :align-self "flex-start"}}
        [:div.coin
          [:div.coin__img.display-icon
            [plus-icon]]
          [:div.coin__content
            [:div.coin__content__main
              [:h6 "Add coin"]]]]]]]))
