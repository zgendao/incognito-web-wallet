(ns app.icons
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            ["react-inlinesvg" :default SVG]))

(defn plus-icon [color]
  [:> SVG {:uniquifyIDs true :src "/images/icons/plus.svg" :style {"--icon-color" (if color color "var(--color-text-default)")}}])

(defn copy-icon [color]
  [:> SVG {:uniquifyIDs true :src "/images/icons/copy.svg" :style {"--icon-color" "#C4C4C4"}}])

(defn qr-code-icon [color]
  [:> SVG {:uniquifyIDs true :src "/images/icons/qr-code.svg" :style {"--icon-color" "#C4C4C4"}}])

(defn account-icon [color]
  [:> SVG {:uniquifyIDs true :src "/images/icons/account.svg" :style {"--icon-color" (if color color "var(--color-text-default)")}}])

(defn arrow-down-icon [color]
  [:> SVG {:uniquifyIDs true :src "/images/icons/arrow-down.svg" :style {"--icon-color" (if color color "var(--color-text-default)")}}])

(defn arrow-right-icon [color]
  [:> SVG {:uniquifyIDs true :src "/images/icons/arrow-right.svg" :style {"--icon-color" (if color color "var(--color-text-default)")}}])

(defn arrow-narrow-right-icon [color]
  [:> SVG {:uniquifyIDs true :src "/images/icons/arrow-narrow-right.svg" :style {"--icon-color" (if color color "var(--color-text-default)")}}])

(defn edit-icon [color]
  [:> SVG {:uniquifyIDs true :src "/images/icons/edit.svg" :style {"--icon-color" (if color color "var(--color-text-default)")}}])

(defn delete-icon [color]
  [:> SVG {:uniquifyIDs true :src "/images/icons/delete.svg" :style {"--icon-color" (if color color "var(--color-text-default)")}}])

(defn infinity-icon [color]
  [:> SVG {:uniquifyIDs true :src "/images/icons/infinity.svg" :style {"--icon-color" (if color color "var(--color-text-default)")}}])

(defn check-icon [color]
  [:> SVG {:uniquifyIDs true :src "/images/icons/check.svg" :style {"--icon-color" (if color color "var(--color-text-default)")}}])

(defn save-icon [color]
  [:> SVG {:uniquifyIDs true :src "/images/icons/save.svg" :style {"--icon-color" (if color color "var(--color-text-default)")}}])

(defn info-icon [color]
  [:> SVG {:uniquifyIDs true :src "/images/icons/info.svg" :style {"--icon-color" (if color color "var(--color-text-default)")}}])

(defn bulb-icon [color]
  [:> SVG {:uniquifyIDs true :src "/images/icons/bulb.svg" :style {"--icon-color" (if color color "var(--color-text-default)")}}])

(defn alert-icon [color]
  [:> SVG {:uniquifyIDs true :src "/images/icons/alert.svg" :style {"--icon-color" (if color color "var(--color-text-default)")}}])

(defn refresh-icon [color]
  [:> SVG {:uniquifyIDs true :src "/images/icons/refresh.svg" :style {"--icon-color" (if color color "var(--color-text-default)")}}])

(defn loader []
  [:span.loader
    [:svg {:viewBox "0 0 100 100" :xmlns "http://www.w3.org/2000/svg" :stroke "var(--color-text-default)"}
      [:circle {:cx "50" :cy "50" :r "45"}]]
    [:h6 "Entering Incognito mode for your crypto"]])
