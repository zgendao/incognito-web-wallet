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
(defn input [form name label type placeholder end-element class]
  [:div.input-group {:class class}
    [:label {:for name} label]
    [:div.input-wrapper
      [:input {:id name :name name :type type :placeholder placeholder
               :class (when end-element "input--withEndElement")
               :style {"--end-element-length" (str (if (string? end-element) (count end-element) "2.5") "em")}
               :value (get-in @state [form name])
               :on-change (fn [e] (swap! state assoc-in [form name] (-> e .-target .-value))
                                  (swap! state assoc-in [form :errors name] nil))}]
      (when end-element
        [:span
          end-element])]
    [:p.input__error (get-in @state [form :errors name])]])

(defn show-error [form name error-message]
  (swap! state assoc-in [form :errors name] error-message))

(defn no-errors? [form]
  (if (empty? (get-in @state [form :errors])) true false))