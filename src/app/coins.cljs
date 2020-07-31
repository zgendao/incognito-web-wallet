(ns app.coins
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [app.storage :refer [state accounts coins]]
            [goog.string :as gstring :refer [format]]
            [goog.string.format]))

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
      [:button.circle-btn {:on-click #(swap! state assoc :selected-coin false)} ">"]]))

(defn coins-container []
  [:div.coins-container {:class [(when (= (@state :selected-coin) "?") "highlighted")]}
    (map (fn [{:keys [id amount]}]
          (coin id amount))
      ((@accounts (@state :selected-account)) :coins))])