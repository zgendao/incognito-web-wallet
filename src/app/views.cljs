(ns app.views
  (:require [reagent.core :as reagent :refer [atom]]
            [app.state :refer [state]]))

(defn app []
  [:div {:style {:display "flex" :justify-content "center" :align-items "center" :flex-direction "column"}}
   [:h1 (:prv-price @state)]
   (if (:wasm-loaded @state)
     [:h1 "Initialized"]
     [:h1 "Loading.."])])
