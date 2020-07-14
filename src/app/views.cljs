(ns app.views
  (:require [reagent.core :as reagent :refer [atom]]))

(defn app []
  [:div {:style {:display "flex" :justify-content "center"}}
   [:h1 "Welcome"]])
