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
  [:> Tippy {:content "Copied" :trigger "click" :theme "skeleton" :zIndex 10000
             :animation "shift-away" :duration #js [150 250] :arrow false
             :onShow (fn [instance] (.setProps instance #js {:trigger "mouseenter"}))
             :onUntrigger (fn [instance] (.setProps instance #js {:trigger "click"}))
             :onTrigger #(copy-to-clipboard address)}
    [:> Tippy {:content "Click to copy" :singleton target :hideOnClick false}
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
  [:> Tippy {:content (reagent/as-element [qr-code address]) :zIndex 10000
             :allowHTML true :trigger "click" :animateFill true :plugins #js [animateFill]
             :onShow (fn [instance] (.setProps instance #js {:trigger "mouseenter"}))
             :onUntrigger (fn [instance] (.setProps instance #js {:trigger "click"}))}
    [:> Tippy {:content "Show QR code" :singleton target :hideOnClick false}
      [:button.inline-icon {:type "button"} [qr-code-icon]]]])