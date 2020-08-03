(ns app.icons
  (:require [reagent.core :as reagent :refer [atom create-class dom-node]]
            [react-svgmt :refer [SvgLoader SvgProxy]]))

(defn qr-code-icon []
  (reagent/create-element       
    SvgLoader
    #js {:path "/images/qr-code.svg"}))

(defn account-icon []
  (reagent/create-element       
    SvgLoader
    #js {:path "/images/account.svg"}))

(defn down-arrow-icon []
  (reagent/create-element       
    SvgLoader
    #js {:path "/images/down-arrow.svg"}))

(defn right-arrow-icon []
  (reagent/create-element       
    SvgLoader
    #js {:path "/images/right-arrow.svg"}))

(defn edit-icon []
  (reagent/create-element       
    SvgLoader
    #js {:path "/images/edit.svg"}))