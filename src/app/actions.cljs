(ns app.actions
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [app.storage :refer [state accounts coins]]
            [app.tabs :refer [tabs-component input show-error no-errors? in-confirm-state? to-confirm-state close-confirm-state]]
            [app.icons :refer [account-icon infinity-icon check-icon]]
            [clojure.string :as str]
            [goog.string :as gstring :refer [format]]
            [goog.string.format]
            ["@tippyjs/react" :default Tippy]))

(defn reset-send-data []
  (swap! state assoc :send-data {:reciepent-address nil
                                  :amount nil
                                  :fee 0.0000001
                                  :note nil
                                  :in-confirm-state false
                                  :sent false}))

(defn get-amount [coin-id]
  (get
    (first
      (filter (fn [{:keys [id amount]}] (= id coin-id))
        (get-in @accounts [(@state :selected-account) :coins])))
    :amount))

(defn send []
  (let [reciepent-address (get-in @state [:send-data :reciepent-address])
        amount (get-in @state [:send-data :amount])]
    (if-not (in-confirm-state? :send-data)
      (do
        (swap! state assoc-in [:send-data :errors] {})
        
        (when (clojure.string/blank? reciepent-address)
          (show-error :send-data :reciepent-address "Please enter the address you want to send to"))
        (if (clojure.string/blank? amount)
          (show-error :send-data :amount "Please set the amount")
          (if (= (double amount) 0)
            (show-error :send-data :amount "The amount must be more than 0")
            (when (> 0.0000001 (get-amount 0))
              (show-error :send-data :amount "Insufficient PRV balance to cover transaction fee (0.0000001 PRV)"))))
          
        (when (no-errors? :send-data) 
          (to-confirm-state :send-data)))
      (if-not (= true (get-in @state [:send-data :sent]))
        (do
          ;backend: send
          (swap! state assoc-in [:send-data :sent] true))
        (do
          (close-confirm-state :send-data)
          (reset-send-data))))))

(defn incognito-address? [address]
  (if (and address (not= address "?"))
    (and (str/starts-with? address "12") (= 103 (count address)))
    true))

(defn set-max-amount []
  (def amount (get-amount (@state :selected-coin)))
  (if (and (= (@state :selected-coin) 0) (> amount 0.0000001))
    (def max-amount (- amount 0.0000001))
    (def max-amount amount))
  (swap! state assoc-in [:send-data :amount] max-amount)
  (swap! state assoc-in [:send-data :errors :amount] nil))

(defn send-data-elem [title content after class]
  [:<>
    [:p {:class class} title]
    [:p {:class class} content " " after]])

(defn send-form []
  (let [incognito? (incognito-address? (get-in @state [:send-data :reciepent-address]))
        confirm-state? (in-confirm-state? :send-data)
        sent-state? (get-in @state [:send-data :sent])]
    [:form.send-wrapper {:class (when confirm-state? "confirm-state")
                         :auto-complete "off"
                         :on-submit (fn [e]
                                      (.preventDefault e)
                                      (send))} ;backend: send function                                   
      [input :send-data :reciepent-address "To" "text" "Enter address (Incognito or external)"
             [[:> Tippy {:content "Select from your accounts" :animation "shift-away"}
                [:button {:type "button" :on-click #(do (swap! state assoc-in [:send-data :reciepent-address] "?")
                                                        (.scrollTo js/window #js {:top 0 :behavior "smooth"}))}
                  [account-icon]]]]]
      [input :send-data :amount "Amount" "number"
              (if (@state :selected-coin) "0" "Select coin first")
              (cond
                (= (@state :selected-coin) "?") ["?"]
                (@state :selected-coin) [[:> Tippy {:content "Set maximum amount" :animation "shift-away"}
                                          [:button {:type "button" :style {:margin-right "15px"}
                                                    :on-click #(set-max-amount)} [infinity-icon]]]
                                         (get-in @coins [(@state :selected-coin) "Symbol"])]
                :else [[:div {:on-click #(swap! state assoc :selected-coin "?") :style {:height "100%" :width "500px"}}]])
              "" (get-amount (@state :selected-coin))]
      [input :send-data :note "Memo" "text" "Add note (optional)" "" (if (or (not incognito?) confirm-state?) "animate-height out" "animate-height in")]
      [:div.btn-wrapper
        [:div
          [:button.btn {:type "submit" :class (when incognito? "btn--primary")} "Send " (if incognito? "anynomously" "(Unshield)")]
          [:div.confirm-background.confirm-background--large {:class (when incognito? "confirm-background--primary")}]]]
      (when confirm-state?
        [:div.confirm-layer
          (if-not sent-state?
            [:h3 "Review your transaction"]
            [:h3.inline-icon [check-icon "white"] "Successfully sent"])
          [:div.send-confirm-data
            [send-data-elem "To: " (get-in @state [:send-data :reciepent-address]) "" "cut-text"]
            [send-data-elem "Amount: " (get-in @state [:send-data :amount]) (get-in @coins [(@state :selected-coin) "Symbol"])]
            (when (and incognito? (not-empty (get-in @state [:send-data :note])))
              [send-data-elem "Note: " (get-in @state [:send-data :note])])
            [send-data-elem "Fee: " (format "%.7f" (double (get-in @state [:send-data :fee]))) " PRV"]
            (when sent-state?
              [send-data-elem "Time: "
                              (.toLocaleString (js/Date.) "en-US" #js {:hour12 false
                                                                       :second "2-digit"
                                                                       :minute "2-digit"
                                                                       :hour "2-digit"
                                                                       :day "2-digit"
                                                                       :month "short"})
                              "" "animate-height in"])]
          (when-not sent-state?
            [:p.text-inverse-soft (if incognito? "Your transaction is confidental." "Unshielding turns your assets public again.")])
          [:div.btn-wrapper
            (when-not sent-state?
              [:button {:type "button" :on-click #(close-confirm-state :send-data)} "Cancel"])
            [:button.btn.btn--inverse {:type "submit" :class (when incognito? "btn--primary")} (if-not sent-state? "Confirm" "Done")]]])]))

(defn actions-container []
  [:div.actions-container
    [tabs-component :actions-tab
      {"Transaction history" :disabled
        "Send" [send-form]}]])
