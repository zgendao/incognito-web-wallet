(ns app.header
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [app.storage :refer [state accounts coins]]
            [app.accounts :refer [get-balance]]
            [app.icons :refer [qr-code-icon down-arrow-icon]]
            [goog.string :as gstring :refer [format]]
            [goog.string.format]
            ["@tippyjs/react" :default Tippy :refer (useSingleton animateFill)]))

(defn key-elem [name key tooltip target]
  [:div.key-elem
    [:> Tippy {:content tooltip :singleton target}
      [:p name]]
    [:div
      [:> Tippy {:content "Click to copy" :singleton target}
        [:a (get-in (@accounts (@state :selected-account)) [:keys key])]]
      [:> Tippy {:content "Show QR code" :singleton target}
        [:button.inline-icon [qr-code-icon]]]]])

;have to use React functional component for the useSingleton hook to work
(defn header__keys-react []
  (let [[source target] (useSingleton #js {:plugins #js ["animateFill"]})]
    (reagent/as-element
      [:<>
        [:> Tippy {:singleton source
                   :delay 200
                   :animateFill true
                   :moveTransition "transform 0.4s cubic-bezier(0.22, 1, 0.36, 1) 0s"}]
        [:div.keys-wrapper
          [:div.keys-modal
            [key-elem "Incognito address:" :incognito "Use it to recieve any coins from another Incognito address" target]
            [:div
              [key-elem "Payment key:" :public "" target]
              [key-elem "Private key:" :private "" target]
              [key-elem "Validator key:" :validator "" target]]]]
        [:> Tippy {:content "Show all keys" :singleton target}
          [:button.circle-btn {:on-click #(swap! state assoc :keys-opened (not (@state :keys-opened)))}
            [down-arrow-icon]]]])))

(defn header []
  [:<>
    [:div.header__amount
      [:div
        [:p "Total shielded balance"]
        [:h1 "$ " (format "%.2f" (get-balance (@accounts (@state :selected-account))))]]
      [:button.btn "+ Shield crypto"]]
    [:div.header__keys {:class [(when (@state :keys-opened) "opened")]}
      [:> header__keys-react]]])
