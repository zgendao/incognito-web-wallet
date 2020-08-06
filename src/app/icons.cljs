(ns app.icons
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            ["react-inlinesvg" :default SVG]))

(defn copy-icon []
  [:> SVG {:uniquifyIDs true :src "/images/copy.svg"}])

(defn qr-code-icon []
  [:> SVG {:uniquifyIDs true :src "/images/qr-code.svg"}])

(defn account-icon []
  [:> SVG {:uniquifyIDs true :src "/images/account.svg"}])

(defn down-arrow-icon []
  [:> SVG {:uniquifyIDs true :src "/images/down-arrow.svg"}])

(defn right-arrow-icon []
  [:> SVG {:uniquifyIDs true :src "/images/right-arrow.svg"}])

(defn edit-icon []
  [:> SVG {:uniquifyIDs true :src "/images/edit.svg"}])