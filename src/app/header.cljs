(ns app.header
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [app.storage :refer [state accounts coins]]
            [app.accounts :refer [get-balance]]
            [app.icons :refer [qr-code-icon down-arrow-icon]]
            [goog.string :as gstring :refer [format]]
            [goog.string.format]
            ["@tippyjs/react" :default Tippy :refer (useSingleton)]))

(defn copy-to-clipboard [val]
  (let [temp (js/document.createElement "textarea")]
    (set! (.-value temp) val)
    (.appendChild js/document.body temp)
    (.select temp)
    (js/document.execCommand "copy")
    (.removeChild js/document.body temp)))

(defn key-elem [name key tooltip target]
  [:div.key-elem
    [:> Tippy {:content tooltip :singleton target}
      [:p name]]
    [:div
      [:> Tippy {:content "Click to copy" :singleton target :hideOnClick false}
        [:a {:on-click #(do (copy-to-clipboard (-> % .-target .-innerHTML)))}
             ;               (set! (.. (.querySelector js/document ".tippy-content") -firstElementChild -innerHTML)
             ;                     "Copied"))}
             ;:on-mouse-out #(set! (.. (.querySelector js/document ".tippy-content") -firstElementChild -innerHTML)
              ;                    "Click to copy"))}
          (get-in (@accounts (@state :selected-account)) [:keys key])]]
      [:> Tippy {:content "Show QR code" :singleton target}
        [:button.inline-icon [qr-code-icon]]]]])

;have to use React functional component for the useSingleton hook to work
(defn header__keys-react []
  (let [[source target] (useSingleton #js {:overrides #js ["hideOnClick"]})]
    (reagent/as-element
      [:<>
        [:> Tippy {:singleton source
                   :animation "shift-away"
                   :moveTransition "transform 0.4s cubic-bezier(0.22, 1, 0.36, 1) 0s"}]
        [:div.keys-wrapper
          [:div.keys-modal
            [key-elem "Incognito address:" :incognito "Use it to recieve any cryptocurrency from another Incognito address" target]
            [:div
              [key-elem "Public key:" :public "todo" target]
              [key-elem "Private key:" :private "Acts like a password for your account. Store it in a safe place!" target]
              [key-elem "Validator key:" :validator "todo" target]]]]
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
