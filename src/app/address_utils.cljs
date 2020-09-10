(ns app.address_utils
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [app.icons :refer [qr-code-icon]]
            ["qrcode" :as qrcode]
            ["@tippyjs/react" :default Tippy :refer (useSingleton)]
            ["tippy.js" :refer (animateFill)]))
      
(defn copy-to-clipboard [val]
  (let [temp (js/document.createElement "textarea")]
    (set! (.-value temp) val)
    (.appendChild js/document.body temp)
    (.select temp)
    (js/document.execCommand "copy")
    (.removeChild js/document.body temp)))

(defn copy-to-clipboard-component [address target content]
  [:> Tippy {:content "Click to copy" :singleton target :trigger "mouseenter" :hideOnClick false}
    [:> Tippy {:content "Copied" :trigger "click" :theme "skeleton" :zIndex 10000
               :animation "shift-away" :duration #js [150 250] :arrow false
               :onShow (fn [instance] (.setProps instance #js {:trigger "mouseenter"}))
               :onUntrigger (fn [instance] (.setProps instance #js {:trigger "click"}))
               :onTrigger #(copy-to-clipboard address)}
      content]])

(defn qr-code [text]
  (create-class
   {:component-did-mount
    (fn [element]
      (qrcode/toCanvas
       (dom-node element)
       text))
    :reagent-render
    (fn []
      [:canvas#canvas])}))

(defn show-qr-code-component [address target]
  [:> Tippy {:content "Show QR code" :singleton target :hideOnClick false}
    [:> Tippy {:content (when (not-empty address) (reagent/as-element [qr-code address]))
               :zIndex 10000 :allowHTML true :trigger "click"
               :animateFill true :plugins #js [animateFill]
               :onShow (fn [instance] (.setProps instance #js {:trigger "mouseenter"}))
               :onUntrigger (fn [instance] (.setProps instance #js {:trigger "click"}))}
      [:button.display-icon {:type "button"} [qr-code-icon]]]])
