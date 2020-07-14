(ns app.views
  (:require [reagent.core :as reagent :refer [atom]]
            [app.api :refer [prv-price]]))

(defn app []
  [:div {:style {:display "flex" :justify-content "center"}}
   [:h1 @prv-price]])
