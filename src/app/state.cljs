(ns app.state
  (:require [reagent.core :as reagent :refer [atom]]
            [alandipert.storage-atom :refer [local-storage]]))

(def state (atom {:ptokens []
                  :prv-price 0
                  :wasm-loaded false
                  :balances {}
                  :following-tokens {}}))

(def local (local-storage (atom {}) :local))
