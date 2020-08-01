(ns app.actions
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [app.storage :refer [state accounts coins]]
            [app.tabs :refer [tabs-component input]]))

(defn send-form []
  [:form.send-wrapper {:on-submit (fn [e]
                                    (.preventDefault e)
                                    ,,,)} ;backend: send function                                   
    [input :reciepent-address "To" "text" "Enter address (Incognito or external)"
            [:button {:type "button" :on-click #(swap! state assoc-in [:send-data :reciepent-address] "?")} "i"]]            
    [input :amount "Amount" "number"
            (if (@state :selected-coin) "0" "Select coin first")
            (cond
              (= (@state :selected-coin) "?") "?"
              (@state :selected-coin) ((@coins (@state :selected-coin)) "Symbol")
              :else [:div {:on-click #(swap! state assoc :selected-coin "?") :style {:height "100%" :width "400px"}}])]
    [input :fee "Fee" "number" "0.00" "PRV"]
    [input :note "Memo" "text" "Add note (optional)"]
    [:div.btn-wrapper
      [:button.btn.btn--primary "Send Anonymously"]]])

(defn actions-container []
  [:div.actions-container
    [:div.actions-wrapper
      [tabs-component :actions-tab
        {"Transaction history" [:p "Transaction history"]
         "Send" [send-form]}]]])
