(ns app.accounts
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [app.state :refer [state]]
            [animate-css-grid :refer [wrapGrid]]))

(defn account-selector [key balance]
  (let [name (str "Account " key)]
    [:div {:key key}
      [:div.account-selector {:on-click (if-not (= (get-in @state [:send-data :reciepent-address]) "?")
                                          #(swap! state assoc :selected-account name)
                                          #(swap! state assoc-in [:send-data :reciepent-address] name))
                              :class [(when (= (@state :selected-account) name) "account-selector--active")
                                      (when (= (get-in @state [:send-data :reciepent-address]) name) "account-selector--reciepent")]}
        [:div
          [:h6.account-selector__name name]
          [:div.account-selector__balance
            [:p balance]]]]]))

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
          (for [x (range 0 10)]
            [account-selector x "300"])])}))

(defn accounts []
  [:div#accounts.container {:class [(when-not (@state :selected-account) "opened opened--full")
                                    (when (= (get-in @state [:send-data :reciepent-address]) "?") "opened opened--half highlighted")]}                          
    [:div.accounts-wrapper
      [accounts-grid]]
    [:button.circle-btn {:on-click #(swap! state assoc :selected-account false)} "i"]])
