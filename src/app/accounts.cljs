(ns app.accounts
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [app.state :refer [state]]
            [animate-css-grid :refer [wrapGrid]]))

(defn account-selector [name balance]
  [:div
    [:div.account-selector {:on-click #(swap! state assoc :selected-account name)
                            :class [(when (= (@state :selected-account) name) "account-selector--active")]}
      [:div
        [:h6.account-selector__name name]
        [:div.account-selector__balance
          [:p balance]]]]])

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
            [account-selector (str "Account " x) "300"])])}))

(defn accounts []
  [:div#accounts.container {:class [(when (@state :selected-account) "collapsed")
                                    (when (@state :reciepent-address) "highlighted")]}                          
    [:div.accounts-wrapper
      [accounts-grid]]
    [:button.circle-btn {:on-click #(swap! state assoc :selected-account false)} "i"]])
