(ns app.tabs
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [app.storage :refer [state]]))

(defn tab-selector [name]
  [:a.tab-btn {:key name
               :on-click #(swap! state assoc :active-tab name)
               :class [(when (= (@state :active-tab) name) "tab-btn--active")]}
    name])

(defn tab [name content]
  [:div.tab {:key name :class [(when (= (@state :active-tab) name) "tab--active")]}
    content])

(defn tabs-component [tabs]
  [:div.tabs-wrapper
    [:div.tabs-pagination
      (for [[k v] tabs]
        [tab-selector k])
      [:div.tabs__background]]
    (for [[k v] tabs]
      [tab k v])])


;input component
(defn input [name label type placeholder end-element]
  [:div.input-group
    [:label {:for name} label]
    [:div.input-wrapper
      [:input {:id name :name name :type type :placeholder placeholder
               :value (get-in @state [:send-data name])
               :on-change #(swap! state assoc-in [:send-data name] (-> % .-target .-value))}]
      (when end-element
        [:span
          end-element])]])