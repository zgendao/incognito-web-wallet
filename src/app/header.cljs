(ns app.header
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [app.storage :refer [state accounts coins]]
            [app.accounts :refer [get-balance]]
            [goog.string :as gstring :refer [format]]
            [goog.string.format]))

(defn key-elem [name key]
  [:div.key-elem
    [:p name]
    [:div
      [:a (get-in (@accounts (@state :selected-account)) [:keys key])]
      [:span "i"]]])

(defn header []
  [:<>
    [:div.header__amount
      [:div
        [:p "Total shielded balance"]
        [:h1 "$ " (format "%.2f" (get-balance (@accounts (@state :selected-account))))]]
      [:button.btn "+ Shield crypto"]]
    [:div.header__keys
      [:div.keys-wrapper
        [:div.keys-modal {:class [(when (@state :keys-opened) "opened")]}
          [key-elem "Incognito address:" :incognito]
          [:div
            [key-elem "Payment key:" :public]
            [key-elem "Private key:" :private]
            [key-elem "Validator key:" :validator]]]]
      [:button.circle-btn {:on-click #(swap! state assoc :keys-opened (not (@state :keys-opened)))}
        "v"]]])
