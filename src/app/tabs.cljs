(ns app.tabs
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [app.storage :refer [state]]
            [app.icons :refer [alert-icon]]
            ["@tippyjs/react" :default Tippy]))

(defn tab-selector [component-state name disabled?]
  [:a.tab-btn {:on-click (when-not disabled? #(swap! state assoc component-state name))
               :class [(when (= (@state component-state ) name) "tab-btn--active")
                       (when disabled? "disabled")]}
    (if disabled?
      [:> Tippy {:content "Not available yet" :animation "shift-away"} [:p name]]
      name)])

(defn tab [component-state name content]
  [:div.tab {:class [(when (= (@state component-state) name) "tab--active")]}
    content])

(defn tabs-component [component-state tabs]
  [:div.tabs-wrapper
    [:div.tabs-pagination
      (for [[name content] tabs]
        ^{:key name} [tab-selector component-state name (= content :disabled)])
      [:div.tabs__background]]
    (for [[name content] tabs]
      ^{:key name} [tab component-state name content])])


;form stuff
(defn get-end-element-length [el]
  (if (string? el) (count el) 2.5))

(defn input [form name label type placeholder end-elements class max]
  (let [error (get-in @state [form :errors name])]
    [:div.input-group {:class class}
      [:label {:for name} label]
      [:div.input-wrapper
        [:input {:id name :name name :type type :placeholder placeholder
                  :class (when end-elements "withEndElement")
                  :style {"--end-element-length" (str (reduce +
                                                        (map (fn [el] (get-end-element-length el))
                                                          end-elements))
                                                    "em")}
                  :value (get-in @state [form name])
                  :max max
                  :on-change (fn [e] (swap! state assoc-in [form name] (-> e .-target .-value)
                                      (swap! state assoc-in [form :errors name] nil)))}]
        (when end-elements
          (into [:span]
            (for [el end-elements]
              el)))]
      [:p.input__error.inline-icon {:class (when error "show")}
        (when error
          [:<> [alert-icon "var(--color-alert)"] error])]]))

(defn show-error [form name error-message]
  (swap! state assoc-in [form :errors name] error-message))

(defn no-errors? [form]
  (empty? (get-in @state [form :errors])))

(defn in-confirm-state? [form]
  (get-in @state [form :in-confirm-state]))

(defn to-confirm-state [form]
  (swap! state assoc-in [form :in-confirm-state] true))

(defn close-confirm-state [form]
  (swap! state assoc-in [form :in-confirm-state] false)
  (swap! state assoc-in [form :sent] false))