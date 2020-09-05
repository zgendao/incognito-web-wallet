(ns app.accounts
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [app.storage :refer [state accounts coins local]]
            [app.wallet :refer [wallet init-wallet create-backup]]
            [app.icons :refer [plus-icon copy-icon qr-code-icon edit-icon delete-icon arrow-narrow-right-icon save-icon]]
            [app.address_utils :refer [show-qr-code-component copy-to-clipboard-component copy-to-clipboard]]
            [app.tabs :refer [tabs-component input show-error no-errors? in-confirm-state? to-confirm-state]]
            [app.actions :refer [reset-send-data]]
            [goog.object :as g]
            [goog.string :as gstring :refer [format]]
            [goog.string.format]
            [async-await.core :refer [async await]]
            ["animate-css-grid" :refer [wrapGrid]]
            ["@tippyjs/react" :default Tippy :refer (useSingleton)]))

(defn create-account []
  (async
   (await (-> (wallet) .-masterAccount (.addAccount (get-in @state [:add-account-data :name]))))
   (create-backup)))

(defn import-account []
  (async
    (await (-> (wallet) .-masterAccount (.importAccount (get-in @state [:add-account-data :name]) (get-in @state [:add-account-data :private-key]))))
    (create-backup)))

(defn get-address [account-index]
  (get-in @accounts [account-index :keys :incognito]))

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

(defn scroll-to-account [account-index]
  (js/setTimeout
    (fn []
      (.scrollTo (.getElementById js/document "accounts-wrapper-inner")
        #js {:left (- (.-offsetLeft (.getElementById js/document (str "account_" account-index))) 100)
             :behavior "smooth"}))
    50))

(defn switch-account [account-index]
  (when-not (= account-index (@state :selected-account))
    (when (@state :add-account-opened) (close-add-account-panel))
    (when (@state :delete-account-opened) (swap! state assoc :delete-account-opened false))
    (swap! state assoc :selected-account account-index)
    (swap! state assoc :selected-coin nil)
    (reset-send-data)
    (def el (.getElementById js/document "accounts-grid"))
    (if account-index
      (do (scroll-to-account account-index)
          (js/setTimeout
            #(.setProperty (.-style el) "width" (str (.-offsetWidth el) "px"))
            400))
      (js/setTimeout
        #(.removeProperty (.-style el) "width")
        400))))

(defn switch-reciepent-account [account-index]
  (when-not (= account-index (@state :selected-account))
    (do
      (swap! state assoc-in [:send-data :reciepent-address] (get-address account-index))
      (swap! state assoc-in [:send-data :errors :reciepent-address] nil)
      (scroll-to-account (@state :selected-account)))))
    
(defn remove-account [account-index]
  (swap! state assoc :delete-account-opened false)
  (swap! accounts #(vec (concat (subvec @accounts 0 account-index) (subvec @accounts (inc account-index)))))
  (-> (wallet) .-masterAccount (.removeAccount (get-in @accounts [account-index :name])))
  (create-backup))

;temporary
(defn rand-str [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))

(defn add-account [import?]
  (let [new-account-name (get-in @state [:add-account-data :name])]
    (if-not (in-confirm-state? :add-account-data)
      (do
        (swap! state assoc-in [:add-account-data :errors] {})
        
        (when (clojure.string/blank? new-account-name)
          (show-error :add-account-data :name "Please enter a name for your account"))
        (when (contains? (set (map :name @accounts)) new-account-name)
          (show-error :add-account-data :name "You already have an account with this name. Please choose another one!"))
        (when import?
          (do
            (when (clojure.string/blank? (get-in @state [:add-account-data :private-key]))
              (show-error :add-account-data :private-key "Please enter the private key of the account you want to import"))
            (doseq [acc (.getAccounts (.-masterAccount (wallet)))]
              (when (= (.-privateKeySerialized (.-keySet (.-key acc))) (get-in @state [:add-account-data :private-key]))
                (show-error :add-account-data :private-key "This account has been already added")))))
    
        (when (no-errors? :add-account-data) 
          (do
            (if import? (import-account) (create-account))
            (to-confirm-state :add-account-data))))
      (close-add-account-panel))))

(defn singleton-buttons-react [key]
  (let [[source target] (useSingleton #js {:overrides #js ["allowHTML" "hideOnClick" "trigger"]})]
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
      [:pre.withEndElement {:style {"--end-element-length" "4em"}} key]
      [:> singleton-buttons-react key]]
    [:div.btn-wrapper
      (when cancel-btn?
        [:button {:type "button" :on-click cancel-fn} "Cancel"])
      [:button.btn.btn--inverse {:type "submit"
                                 :on-click submit-fn}
        submit-text]]])

(defn add-account-tab [import?]
  [:form {:class (when (in-confirm-state? :add-account-data) "confirm-state")
          :auto-complete "off"
          :on-submit (fn [e] (.preventDefault e)
                             (add-account import?))}
    [input :add-account-data :name "Name" "text" "Name your account"]
    [input :add-account-data :private-key "Private key" "text" "Paste from your phone" ""
           (str "animate-height" (when-not (or import? (in-confirm-state? :add-account-data)) " out"))]
    [:div.btn-wrapper
      [:button {:type "button" :on-click #(close-add-account-panel)} "Cancel"]
      [:div
        [:button.btn {:type "submit"} (if import? "Import" "Create") " account"]
        [:div.confirm-background.confirm-background--medium]]]
    (when (in-confirm-state? :add-account-data)
      [save-private-key-confirm-layer
        "Make sure you won't lose access!"
        [:p "Accounts are only saved locally in your browser, so if anyhow the site data gets deleted, they vanish.
             The only way you can restore them is by their private key, so always save them in a safe place."]
        (.-privateKeySerialized (.-keySet (.-key (.pop (.slice (.getAccounts (.-masterAccount (wallet))) -1)))))
        "I'm safe"
        #(init-wallet)])])
        
(defn add-account-panel []
  [:div.add-account-wrapper {:style {:order 100}
                             :on-click (when-not (@state :add-account-opened)
                                          #(swap! state assoc :add-account-opened true))
                             :class (when (@state :add-account-opened) "opened")}
    [:div.add-account
      [:h4.inline-icon.add-account__title
        [plus-icon "var(--color-text-default-soft)"] "Add account"]
      [tabs-component :add-account-tab
        {"Create new" [add-account-tab false]
         "Import existing" [add-account-tab true]}]]])
         ;"Bulk import" ""}]]])

(defn account-selector [account]
  (let [index (.indexOf @accounts account)
        name (get account :name)
        balance (get-balance account)
        selected-account-index (@state :selected-account)
        reciepent? (reciepent-address? (get-address index))
        delete-opened? (= (@state :delete-account-opened) index)]
    [:div.account-selector
      {:id (str "account_" index)
       :style {"--default-order" index
               "--after-selected-account" (if (< index selected-account-index)
                                           (inc selected-account-index)
                                           selected-account-index)}
       :class [(when (= selected-account-index index) "account-selector--active")
               (when reciepent? "account-selector--reciepent")
               (when delete-opened? "confirm-state")]}
      [:div
        (when reciepent?
          [:button.account-selector--reciepent__arrow.display-icon {:on-click #(switch-reciepent-account nil)}
            [arrow-narrow-right-icon]])
        [:div.account-selector__inner
          {:on-click (when-not delete-opened?
                      (if (reciepent-address? "?")
                        #(switch-reciepent-account index)
                        #(switch-account index)))}
          [:div
            [:h6.account-selector__name name]
            [:div.account-selector__balance
              [:p (format "%.2f" balance) " USD"]]
            [:div.account-selector__icon
              [:> Tippy {:content "Remove account" :animation "shift-away"}
                [:button.display-icon {:type "button"
                                        :on-click (fn [e] (.stopPropagation e)
                                                          (swap! state assoc :delete-account-opened index))}
                  [delete-icon]]]
              [:div.confirm-background.confirm-background--large]]]
          (when delete-opened?
            [save-private-key-confirm-layer
              "Are you sure? Don't lose access to your money!"
              (reagent/as-element
                [:p "Only remove " [:b name] " if you have your private keys saved in a safe place,
                      so you can restore it later or import it in another wallet."])
              (get-in account [:keys :private])
              "I know what I'm doing"
              #(remove-account index)
              true
              #(swap! state assoc :delete-account-opened false)])]]]))

(defn horizontal-scroll []
  (def container (.getElementById js/document "accounts-wrapper-inner"))
  (.addEventListener container "wheel"
    (fn [e]
      (when (and (@state :selected-account) (not= "?" (get-in @state [:send-data :reciepent-address])) (not= (.-deltaY e) 0))
        (.preventDefault e)
        (def containerScrollPosition (.-scrollLeft container))
        (def newScrollPosition (+ containerScrollPosition (if (neg? (.-deltaY e)) -100 100)))
        (.scrollTo container
          #js {:left newScrollPosition})))
    #js {:passive false})
  (.addEventListener container "scroll"
    (fn [e]
      (if (= (.-scrollLeft container) 0)
        (set! (.. (.getElementById js/document "accounts") -dataset -scrolled) false)
        (set! (.. (.getElementById js/document "accounts") -dataset -scrolled) true)))))

(defn accounts-grid []
  (create-class
    {:component-did-mount
      (fn []
        (do
          (def grid (.getElementById js/document "accounts-grid"))
          (wrapGrid grid #js {:duration 400})
          (horizontal-scroll)))
     :reagent-render
      (fn []
        ;js-selectReciepent class is only used to trigger animate-css-grid (could add any class)
        [:div#accounts-grid.accounts-grid {:class (when (reciepent-address? "?") "js-selectReciepent")}
          (into [:<>]
            (for [account @accounts]
              ^{:key (get account :name)} [account-selector account]))
          [add-account-panel]])}))

(defn backup-all-accounts []
  (let [content (reduce str (for [account @accounts]
                              (str "AccountName: " (get account :name) "\nPrivateKey: " (get-in account [:keys :private]) "\n\n")))]
    [:div.tippy-content-wrapper
      [:p "You can restore all saved accounts by the data below. Make sure to store it in a safe place!"]
      [:div.input-wrapper
        [:pre content]]
      [:div.btn-wrapper
        [:button#backup-copy.btn.inline-icon {:on-click #(do (copy-to-clipboard content)
                                                             (-> % .-target (.setAttribute "data-copied" true)))}
          [copy-icon] "Copy to clipboard"]]]))

(defn backup-tooltip []
  (let [hover-instance (@state :backup-tippy-instance)]
    [:> Tippy {:content "Backup all accounts" :placement "left-start"
               :hideOnClick false :zIndex 1 :interactive true
               :onCreate (fn [instance] (swap! state assoc :backup-tippy-instance instance))}
      [:> Tippy {:content (reagent/as-element [backup-all-accounts]) :interactive true
                 :maxWidth 525 :placement "left-start" :arrow true
                 :allowHTML true :trigger "click" :animation "height"
                 :onShow (fn [] (.setProps hover-instance #js {:trigger "click"}))
                 :onHide (fn [] (.hide hover-instance)
                                (.setProps hover-instance #js {:trigger "mouseenter"})
                                (.removeAttribute (.getElementById js/document "backup-copy") "data-copied"))}      
        [:button.display-icon [save-icon]]]]))

(defn accounts-container []
  [:div#accounts.container {:class [(when-not (@state :selected-account) "opened opened--full")
                                    (when (reciepent-address? "?") "opened opened--half highlighted")]
                            :data-scrolled false}
    [:div.accounts-header.accounts-header--manage
      [:h2 "Your accounts"]
      [backup-tooltip]]
    [:div.accounts-header.accounts-header--reciepent
      [:h2 "Select the account you want to send to"]]
    [:div#accounts-wrapper.accounts-wrapper
      [:div#accounts-wrapper-inner.accounts-wrapper-inner
        [accounts-grid]]]
    [:> Tippy {:content "Manage accounts" :animation "shift-away"}
      [:button.circle-btn {:on-click (fn [] (switch-account nil))} [edit-icon]]]])