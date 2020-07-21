(ns app.state
  (:require [reagent.core :as reagent :refer [atom]]
            [alandipert.storage-atom :refer [local-storage]]))

(def state (atom {:prv-price 0
                  :wasm-loaded false}))

(def local (local-storage (atom {}) :prefs))
