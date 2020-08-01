(ns app.tabs
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [app.storage :refer [state]]))

(defn tab-selector [component-state name]
  [:a.tab-btn {:key name
               :on-click #(swap! state assoc component-state name)
               :class [(when (= (@state component-state ) name) "tab-btn--active")]}
    name])

(defn tab [component-state name content]
  [:div.tab {:key name :class [(when (= (@state component-state) name) "tab--active")]}
    content])

(defn tabs-component [component-state tabs]
  [:div.tabs-wrapper
    [:div.tabs-pagination
      (for [[k v] tabs]
        [tab-selector component-state k])
      [:div.tabs__background]]
    (for [[k v] tabs]
      [tab component-state k v])])


;input component
(defn input [name label type placeholder end-element class]
  [:div.input-group {:class class}
    [:label {:for name} label]
    [:div.input-wrapper
      [:input {:id name :name name :type type :placeholder placeholder
               :value (get-in @state [:send-data name])
               :on-change #(swap! state assoc-in [:send-data name] (-> % .-target .-value))}]
      (when end-element
        [:span
          end-element])]])