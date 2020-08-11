(ns app.icons
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            ["react-inlinesvg" :default SVG]))

(defn plus-icon [color]
  [:> SVG {:uniquifyIDs true :src "/images/plus.svg" :style {"--icon-color" color}}])

(defn copy-icon [color]
  [:> SVG {:uniquifyIDs true :src "/images/copy.svg" :style {"--icon-color" "#C4C4C4"}}])

(defn qr-code-icon [color]
  [:> SVG {:uniquifyIDs true :src "/images/qr-code.svg" :style {"--icon-color" "#C4C4C4"}}])

(defn account-icon [color]
  [:> SVG {:uniquifyIDs true :src "/images/account.svg" :style {"--icon-color" "black"}}])

(defn down-arrow-icon [color]
  [:> SVG {:uniquifyIDs true :src "/images/down-arrow.svg" :style {"--icon-color" "black"}}])

(defn right-arrow-icon [color]
  [:> SVG {:uniquifyIDs true :src "/images/right-arrow.svg" :style {"--icon-color" "black"}}])

(defn edit-icon [color]
  [:> SVG {:uniquifyIDs true :src "/images/edit.svg" :style {"--icon-color" "black"}}])

(defn delete-icon [color]
  [:> SVG {:uniquifyIDs true :src "/images/delete.svg" :style {"--icon-color" "black"}}])

(defn infinity-icon [color]
  [:> SVG {:uniquifyIDs true :src "/images/infinity.svg" :style {"--icon-color" "black"}}])