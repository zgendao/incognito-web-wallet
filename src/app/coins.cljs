(ns app.coins
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [app.storage :refer [state accounts coins]]
            [app.icons :refer [right-arrow-icon plus-icon]]
            [goog.string :as gstring :refer [format]]
            [goog.string.format]
            ["@tippyjs/react" :default Tippy]))

(defn coin [id amount]
  (let [name (get-in @coins [id "Name"])
        symbol (get-in @coins [id "Symbol"])
        priceInUSD (get-in @coins [id "PriceUsd"])]
    [:div.coin-wrapper {:class [(when (= (@state :selected-coin) id) "selected")]}
      [:div.coin {:on-click #(swap! state assoc :selected-coin id)}
        [:div.coin__img
          [:img {:src (str "./images/coinLogos/" symbol ".png")}]]
        [:div.coin__content
          [:div.coin__content__main
            [:h6 name]
            [:p amount]]
          [:div.coin__content__prv
            [:p "$" priceInUSD]
            [:p "$" (format "%.2f" (* amount priceInUSD))]]]]
      [:button.circle-btn {:on-click (fn [] (swap! state assoc :selected-coin nil)
                                            (swap! state assoc-in [:send-data :amount] nil))}
        [right-arrow-icon]]]))

(defn coins-container []
  (into [:div.coins-container {:class [(when (= (@state :selected-coin) "?") "highlighted")]}]
    [(map (fn [{:keys [id amount]}]
            ^{:key id} [coin id amount])
        (get-in @accounts [(@state :selected-account) :coins] []))
     [:> Tippy {:content "Not available yet" :interactive true :placement "top-start" :animation "shift-away"}
      [:div.coin-wrapper.disabled {:style {:width "auto" :align-self "flex-start"}}
        [:div.coin
          [:div.coin__img.display-icon
            [plus-icon "black"]]
          [:div.coin__content
            [:div.coin__content__main
              [:h6 "Add coin"]]]]]]]))
