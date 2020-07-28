(ns app.core
  (:require [reagent.core :as r]
            [app.views :as views]
            [app.api :as api]
            [app.state :refer [local]]
            ["incognito-js" :as incognito-js]))

(defn ^:dev/after-load start []
  (if (:backupkey @local)
    (.then
     (incognito-js/WalletInstance.restore (:backupkey @local) (:pw @local))
     (fn [w] (r/render-component [views/app w]
                                 (.getElementById js/document "app"))))
    (r/render-component [views/app (incognito-js/WalletInstance.)]
                        (.getElementById js/document "app"))))

(defn ^:export main []
  (start)
  (api/price-request)
  (api/init-incognito))
