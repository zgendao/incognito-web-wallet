(ns app.core
  (:require [reagent.core :as r]
            [app.views :as views]
            [app.api :as api]
            ["incognito-js" :as incognito-js]))


(defn ^:dev/after-load start []
  (incognito-js/storageService.implement (clj->js {:setMethod #(println (str %1 %2)) :getMethod #(println "ez a getter " %) :removeMethod #(println %)}))
  (r/render-component [views/app]
                      (.getElementById js/document "app")))

(defn ^:export main []
    (start)
    (api/price-request)
    (api/init-incognito))
