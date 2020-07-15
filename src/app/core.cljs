(ns app.core
  (:require [reagent.core :as r]
            [app.views :as views]
            [app.api :as api]))

(defn ^:dev/after-load start []
  (r/render-component [views/app]
                      (.getElementById js/document "app")))

(defn ^:export main []
  (start)
  (api/price-request)
  (api/init-incognito))
