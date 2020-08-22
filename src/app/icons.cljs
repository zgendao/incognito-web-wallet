(ns app.icons
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            ["react-inlinesvg" :default SVG]))

(defn plus-icon [color]
  [:> SVG {:uniquifyIDs true :src "/public/images/icons/plus.svg" :style {"--icon-color" color}}])

(defn copy-icon [color]
  [:> SVG {:uniquifyIDs true :src "/public/images/icons/copy.svg" :style {"--icon-color" "#C4C4C4"}}])

(defn qr-code-icon [color]
  [:> SVG {:uniquifyIDs true :src "/public/images/icons/qr-code.svg" :style {"--icon-color" "#C4C4C4"}}])

(defn account-icon [color]
  [:> SVG {:uniquifyIDs true :src "/public/images/icons/account.svg" :style {"--icon-color" "black"}}])

(defn down-arrow-icon [color]
  [:> SVG {:uniquifyIDs true :src "/public/images/icons/down-arrow.svg" :style {"--icon-color" "black"}}])

(defn right-arrow-icon [color]
  [:> SVG {:uniquifyIDs true :src "/public/images/icons/right-arrow.svg" :style {"--icon-color" "black"}}])

(defn edit-icon [color]
  [:> SVG {:uniquifyIDs true :src "/public/images/icons/edit.svg" :style {"--icon-color" "black"}}])

(defn delete-icon [color]
  [:> SVG {:uniquifyIDs true :src "/public/images/icons/delete.svg" :style {"--icon-color" "black"}}])

(defn infinity-icon [color]
  [:> SVG {:uniquifyIDs true :src "/public/images/icons/infinity.svg" :style {"--icon-color" "black"}}])

(defn check-icon [color]
  [:> SVG {:uniquifyIDs true :src "/public/images/icons/check.svg" :style {"--icon-color" color}}])

(defn loader []
  [:span.loadingScreen
    [:svg {:viewBox "0 0 100 100" :xmlns "http://www.w3.org/2000/svg" :stroke "black"}
      [:circle {:cx "50" :cy "50" :r "45"}]]])