(ns app.accounts
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [app.storage :refer [state accounts coins]]
            [app.icons :refer [edit-icon]]
            [goog.string :as gstring :refer [format]]
            [goog.string.format]
            [app.tabs :refer [tabs-component input show-error no-errors?]]
            [animate-css-grid :refer [wrapGrid]]
            ["@tippyjs/react" :default Tippy]))

(defn get-address [key]
  (get-in @accounts [key :keys :incognito]))

(defn reciepent-address? [address]
  (= (get-in @state [:send-data :reciepent-address]) address))

(defn get-balance [account]
  (reduce +
    (map
      (fn [{:keys [id amount]}]
        (* amount (get-in @coins [id "PriceUsd"])))
      (account :coins))))

(defn close-add-account-panel []
  (swap! state assoc :add-account-opened false)
  (swap! state assoc :add-account-data {:name nil
                                        :private-key nil
                                        :errors {}}))

(defn switch-account [to]
  (when (@state :add-account-opened) (close-add-account-panel))
  (swap! state assoc :selected-account to)
  (swap! state assoc :selected-coin nil)
  (swap! state assoc :send-data {:reciepent-address nil
                                  :amount nil
                                  :fee nil
                                  :note nil}))

;temporary
(defn rand-str [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))

(defn add-account [import?]
  (swap! state assoc-in [:add-account-data :errors] {})
  
  (when (clojure.string/blank? (get-in @state [:add-account-data :name]))
    (show-error :add-account-data :name "Please enter a name for your account"))
  (when (contains? (set (map :name @accounts)) (get-in @state [:add-account-data :name]))
    (show-error :add-account-data :name "You already have an account with this name. Please choose another one!"))
  (when import?
    (when (clojure.string/blank? (get-in @state [:add-account-data :private-key]))
      (show-error :add-account-data :private-key "Please enter the private key of the account you want to import")))
  
  (when (no-errors? :add-account-data) 
    (do
      ,,, ;backend: generate or import
      (swap! accounts conj {:name (get-in @state [:add-account-data :name])
                            :keys {:incognito (rand-str 100)
                                    :public (rand-str 100)
                                    :private (rand-str 100)
                                    :readonly (rand-str 100)
                                    :validator (rand-str 100)}
                            :coins [{:id 0
                                      :amount 0}
                                    {:id (+ 1 (rand-int 5))
                                      :amount (rand-int 100)}]})
      (close-add-account-panel))))

(defn add-account-tab [import?]
  [:form {:on-submit (fn [e] (.preventDefault e)
                             (add-account import?))}
    [input :add-account-data :name "Name" "text" "Name your wallet"]
    [input :add-account-data :private-key "Private key" "text" "Paste from your phone" "" (str "animate-height" (when-not import? " out"))]
    [:div.btn-wrapper
      [:button {:type "button" :on-click #(close-add-account-panel)} "Cancel"]
      [:button.btn {:type "submit"} (if import? "Import" "Create") " wallet"]]])

(defn add-account-panel []
  [:div.add-account-wrapper {:style {:order 100}
                             :on-click (when-not (@state :add-account-opened)
                                          #(swap! state assoc :add-account-opened true))
                             :class (when (@state :add-account-opened) "opened")}
    [:div.add-account
        [tabs-component :add-account-tab
          {"Create new" [add-account-tab false]
           "Import existing" [add-account-tab true]}]]])

(defn account-selector [account]
  (let [key (.indexOf @accounts account)
        name (account :name)
        balance (get-balance account)]
    [:div.account-selector {:key key
                            :style {"--default-order" key
                                    "--after-selected-account" (if (< key (@state :selected-account))
                                                                (inc (@state :selected-account))
                                                                (@state :selected-account))}
                            :class [(when (= (@state :selected-account) key) "account-selector--active")
                                    (when (reciepent-address? (get-address key)) "account-selector--reciepent")]}
      [:div {:on-click (if (reciepent-address? "?")
                          (when-not (= key (@state :selected-account))
                            #(swap! state assoc-in [:send-data :reciepent-address] (get-address key)))
                          #(switch-account key))} 
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
        (into [:div.accounts-grid]
          [
            (for [account @accounts]
              [account-selector account])
            [add-account-panel]]))}))

(defn accounts-container []
  [:div#accounts.container {:class [(when-not (@state :selected-account) "opened opened--full")
                                    (when (reciepent-address? "?") "opened opened--half highlighted")]}
    [:h2.accounts__text "Select the account you want to send to"]
    [:div.accounts-wrapper
      [accounts-grid]]
    [:> Tippy {:content "Show and edit all accounts"}
      [:button.circle-btn {:on-click (fn [] (switch-account nil))} [edit-icon]]]])
