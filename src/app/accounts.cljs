(ns app.accounts
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [app.storage :refer [state accounts coins]]
            [app.icons :refer [plus-icon copy-icon qr-code-icon edit-icon delete-icon]]
            [app.address_utils :refer [show-qr-code-component copy-to-clipboard-component]]
            [app.tabs :refer [tabs-component input show-error no-errors? in-confirm-state? to-confirm-state]]
            [app.actions :refer [reset-send-data]]
            [goog.string :as gstring :refer [format]]
            [goog.string.format]
            [animate-css-grid :refer [wrapGrid]]
            ["@tippyjs/react" :default Tippy :refer (useSingleton)]))

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
                                        :errors {}
                                        :in-confirm-state false}))

(defn switch-account [to]
  (when (@state :add-account-opened) (close-add-account-panel))
  (when (@state :delete-account-opened) (swap! state assoc :delete-account-opened false))
  (swap! state assoc :selected-account to)
  (swap! state assoc :selected-coin nil)
  (reset-send-data))

(defn remove-account [key]
  ;(swap! accounts #(vec (concat (subvec @accounts 0 key) (subvec @accounts (inc key))))))
  (swap! accounts #(dissoc @accounts key)))

(defn switch-reciepent-account [to]
  (when-not (= to (@state :selected-account))
    (do
      (swap! state assoc-in [:send-data :reciepent-address] (get-address to))
      (swap! state assoc-in [:send-data :errors :reciepent-address] nil))))

;temporary
(defn rand-str [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))

(defn add-account [import?]
  (if-not (in-confirm-state? :add-account-data)
    (do
      (swap! state assoc-in [:add-account-data :errors] {})
      
      (when (clojure.string/blank? (get-in @state [:add-account-data :name]))
        (show-error :add-account-data :name "Please enter a name for your account"))
      (when (contains? (set (keys @accounts)) (get-in @state [:add-account-data :name]))
        (show-error :add-account-data :name "You already have an account with this name. Please choose another one!"))
      (when import?
        (when (clojure.string/blank? (get-in @state [:add-account-data :private-key]))
          (show-error :add-account-data :private-key "Please enter the private key of the account you want to import")))
      
      (when (no-errors? :add-account-data) 
        (do
          ,,, ;backend: generate or import
          (to-confirm-state :add-account-data))))
    (do
      (swap! accounts assoc (get-in @state [:add-account-data :name])
                            {:keys {:incognito (rand-str 100)
                                    :public (rand-str 100)
                                    :private (rand-str 100)
                                    :readonly (rand-str 100)
                                    :validator (rand-str 100)}
                             :coins [{:id 0
                                       :amount 0}
                                     {:id (+ 1 (rand-int 5))
                                       :amount (rand-int 100)}]})
      (close-add-account-panel))))

(defn singleton-buttons-react [key]
  (let [[source target] (useSingleton #js {:overrides #js ["allowHTML hideOnClick"]})]
    (reagent/as-element
      [:span
        [:> Tippy {:singleton source
                   :animation "shift-away"
                   :delay #js [0 100]
                   :moveTransition "transform 0.4s cubic-bezier(0.22, 1, 0.36, 1) 0s"}]
        [copy-to-clipboard-component (.-children key) target
          [:button {:type "button"} [copy-icon]]]
        [show-qr-code-component (.-children key) target]])))
  
(defn save-private-key-confirm-layer [title desc key submit-text submit-fn cancel-btn? cancel-fn]
  [:div.confirm-layer
    [:h3.marginTop-0 title]
    desc
    [:div.input-wrapper
      [:pre {:style {"--end-element-length" "4em"}} key]
      [:> singleton-buttons-react key]]
    [:div.btn-wrapper
      (when cancel-btn?
        [:button {:type "button" :on-click cancel-fn} "Cancel"])
      [:button.btn.btn--inverse {:type "submit"
                                 :on-click submit-fn}
        submit-text]]])

(defn add-account-tab [import?]
  [:form {:class (when (in-confirm-state? :add-account-data) "confirm-state")
          :on-submit (fn [e] (.preventDefault e)
                             (add-account import?))}
    [input :add-account-data :name "Name" "text" "Name your wallet"]
    [input :add-account-data :private-key "Private key" "text" "Paste from your phone" ""
           (str "animate-height" (when-not (or import? (in-confirm-state? :add-account-data)) " out"))]
    [:div.btn-wrapper
      [:button {:type "button" :on-click #(close-add-account-panel)} "Cancel"]
      [:div
        [:button.btn {:type "submit"} (if import? "Import" "Create") " wallet"]
        [:div.confirm-background.confirm-background--medium]]]
    (when (in-confirm-state? :add-account-data)
      [save-private-key-confirm-layer
        "Make sure you won't lose access!"
        [:p "Accounts are only saved locally in your browser, so if anyhow the site data gets deleted, they vanish.
             The only way you can restore them is by their private key, so always save them in a safe place."]
        "112t8rnXDhcqHHE9nU6wfcSFvYMtCcQGh6bJRp9BMpY9PBNSHZnwee3Po4XF9yFTwQaa9c6gA9sky8DfPzSRjhr23jWjDsEkzxHuiFFaWWuC"
        "I'm safe"])])
        
(defn add-account-panel []
  [:div.add-account-wrapper {:style {:order 100}
                             :on-click (when-not (@state :add-account-opened)
                                          #(swap! state assoc :add-account-opened true))
                             :class (when (@state :add-account-opened) "opened")}
    [:div.add-account
      [:h4.inline-icon.add-account__title
        [plus-icon] "Add account"]
      [tabs-component :add-account-tab
        {"Create new" [add-account-tab false]
         "Import existing" [add-account-tab true]}]]])

(defn get-order-number [account-name]
  (.indexOf (keys @accounts) account-name))

(defn account-selector [name account]
  (let [self-order (get-order-number name)
        selected-account-order (get-order-number (@state :selected-account))
        balance (get-balance account)]
    [:div.account-selector {:style {"--default-order" self-order
                                    "--after-selected-account" (if (< self-order selected-account-order)
                                                                (inc selected-account-order)
                                                                selected-account-order)}
                            :class [(when (= (@state :selected-account) name) "account-selector--active")
                                    (when (reciepent-address? (get-address name)) "account-selector--reciepent")
                                    (when (= (@state :delete-account-opened) name) "confirm-state")]}
      [:div {:on-click (when-not (= (@state :delete-account-opened) name)
                          (if (reciepent-address? "?")
                            #(switch-reciepent-account name)
                            #(switch-account name)))}
        [:div
          [:h6.account-selector__name name]
          [:div.account-selector__balance
            [:p (format "%.2f" balance) " USD"]]
          [:div.account-selector__icon
            [:> Tippy {:content "Remove account" :animation "shift-away"}
              [:button.display-icon {:type "button"
                                      :on-click (fn [e] (.stopPropagation e)
                                                        (swap! state assoc :delete-account-opened name))}
                [delete-icon]]]
            [:div.confirm-background.confirm-background--large]]]
        (when (= (@state :delete-account-opened) name)
          [save-private-key-confirm-layer
            "Are you sure? Don't lose access to your money!"
            (reagent/as-element
              [:p "Only remove " [:b name] " if you have your private keys saved in a safe place,
                    so you can restore it later or import it in another wallet."])
            (get-in @accounts [name :keys :private])
            "I know what I'm doing"
            #(remove-account name)
            true
            #(swap! state assoc :delete-account-opened false)])]]))

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
            (for [[name data] @accounts]
              ^{:key name} [account-selector name data])
            [add-account-panel]]))}))

(defn accounts-container []
  [:div#accounts.container {:class [(when-not (@state :selected-account) "opened opened--full")
                                    (when (reciepent-address? "?") "opened opened--half highlighted")]}
    [:h2.accounts__text "Select the account you want to send to"]
    [:div.accounts-wrapper
      [accounts-grid]]
    [:> Tippy {:content "Manage accounts" :animation "shift-away"}
      [:button.circle-btn {:on-click (fn [] (switch-account nil))} [edit-icon]]]])
