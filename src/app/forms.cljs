(ns app.forms
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [app.state :refer [state]]))

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

(defn send-form []
  [:form.send-wrapper {:on-submit (fn [e] (.preventDefault e))} ;backend: send function
    [input :reciepent-address "To" "text" "Enter address (Incognito or external)"
            [:button {:type "button" :on-click #(swap! state assoc-in [:send-data :reciepent-address] "?")} "i"]]            
    [input :amount "Amount" "number"
            (if (@state :selected-coin) "0" "Select coin first")
            (if (@state :selected-coin)
              (@state :selected-coin)
              [:div {:on-click #(swap! state assoc :selected-coin "?") :style {:height "100%" :width "400px"}}])]
    [input :fee "Fee" "number" "0.00" "PRV"]
    [input :note "Memo" "text" "Add note (optional)"]
    [:div.btn-wrapper
      [:button.btn.btn--primary "Send Anonymously"]]])
