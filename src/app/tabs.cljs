(ns app.tabs
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [app.state :refer [state]]))

(defn tab-selector [name]
  [:a.tab-btn {:on-click #(swap! state assoc :active-tab name)
               :class [(when (= (@state :active-tab) name) "tab-btn--active")]}
    name])

(defn tab [name content]
  [:div.tab {:class [(when (= (@state :active-tab) name) "tab--active")]}
    content])

(defn tabs-component [tabs]
  [:div.tabs-wrapper
    [:div.tabs-pagination
      (for [[k v] tabs]
        [tab-selector k])
      [:div.tabs__background]]
    (for [[k v] tabs]
      [tab k v])])