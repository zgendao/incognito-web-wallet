(ns app.accounts
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [app.storage :refer [state accounts coins]]
            [goog.string :as gstring :refer [format]]
            [goog.string.format]
            [animate-css-grid :refer [wrapGrid]]))

(defn get-balance [account]
  (reduce +
    (map
      (fn [{:keys [id amount]}]
        (* amount (get-in @coins [id "PriceUsd"])))
      (account :coins))))

(defn account-selector [account]
  (let [key (.indexOf @accounts account)
        name (account :name)
        balance (get-balance account)]
    [:div {:key key :style {:order key}}
      [:div.account-selector {:on-click (if-not (= (get-in @state [:send-data :reciepent-address]) "?")
                                          #(swap! state assoc :selected-account key)
                                          #(swap! state assoc-in [:send-data :reciepent-address] key))
                              :class [(when (= (@state :selected-account) key) "account-selector--active")
                                      (when (= (get-in @state [:send-data :reciepent-address]) key) "account-selector--reciepent")]}
        [:div
          [:h6.account-selector__name name]
          [:div.account-selector__balance
            [:p (format "%.2f" balance) " USD"]]]]]))

(defn accounts-grid []
  (create-class
    {:component-did-mount
      (fn []
        (do
          (def grid (.querySelector js/document ".accounts-grid"))
          (wrapGrid grid #js {:duration 500})))
     :reagent-render
      (fn []
        [:div.accounts-grid
          (for [account @accounts]
            [account-selector account])])}))

(defn accounts-container []
  [:div#accounts.container {:class [(when-not (@state :selected-account) "opened opened--full")
                                    (when (= (get-in @state [:send-data :reciepent-address]) "?") "opened opened--half highlighted")]}                          
    [:div.accounts-wrapper
      [accounts-grid]]
    [:button.circle-btn {:on-click #(swap! state assoc :selected-account false)} "i"]])
