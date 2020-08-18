(ns app.header
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [app.storage :refer [state accounts coins]]
            [app.accounts :refer [get-balance]]
            [app.icons :refer [plus-icon qr-code-icon down-arrow-icon]]
            [app.address_utils :refer [show-qr-code-component copy-to-clipboard-component]]
            [goog.string :as gstring :refer [format]]
            [goog.string.format]
            ["@tippyjs/react" :default Tippy :refer [useSingleton]]))

(defn key-elem [name key tooltip target]
  (let [address (get-in @accounts [(@state :selected-account) :keys key] "")]
    [:div.key-elem
      [:> Tippy {:content tooltip :singleton target}
        [:p name]]
      [:div
        [copy-to-clipboard-component address target [:a.cut-text address]]
        [show-qr-code-component address target]]]))

;have to use React functional component for the useSingleton hook to work
(defn header__keys-react []
  (let [[source target] (useSingleton #js {:overrides #js ["hideOnClick" "allowHTML"]})]
    (reagent/as-element
      [:<>
        [:> Tippy {:singleton source
                   :animation "shift-away"
                   :delay #js [0 100]
                   :moveTransition "transform 0.4s cubic-bezier(0.22, 1, 0.36, 1) 0s"}]
        [:div.keys-wrapper
          [:div.keys-modal
            [key-elem "Incognito address:" :incognito
                      "Use it to recieve any cryptocurrency from another Incognito address." target]
            [:div
              [key-elem "Public key:" :public
                        "Used for authentication and can be shared with third-party services." target]
              [key-elem "Private key:" :private
                        "Acts like a password for your account.
                         It's the only way to regain access to your funds in case of device loss,
                         so store it in a safe place!" target]
              [key-elem "Validator key:" :validator
                        "Used by Node owners. This is what an account can get assigned to a Node by,
                         so it'll use that account for staking and withdrawing earnings." target]]]]
        [:> Tippy {:content (if (@state :keys-opened) "Hide" "Show keys") :singleton target}
          [:button.circle-btn {:on-click #(swap! state assoc :keys-opened (not (@state :keys-opened)))}
            [down-arrow-icon]]]])))

(defn header []
  [:<>
    [:div.header__amount
      [:div
        [:p "Total shielded balance"]
        [:h1 "$ " (format "%.2f" (get-balance (get @accounts (@state :selected-account) {})))]]
      [:> Tippy {:content "Not available yet" :animation "shift-away"}
        [:div.disabled
          [:button.btn.inline-icon {:disabled true}
            [plus-icon "white"] "Shield crypto"]]]]
    [:div.header__keys {:class [(when (@state :keys-opened) "opened")]}
      [:> header__keys-react]]])
