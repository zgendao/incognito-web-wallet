(ns app.state
  (:require [reagent.core :as reagent :refer [atom]]
            [alandipert.storage-atom :refer [local-storage]]))

(def state (atom {:prv-price 0
                  :wasm-loaded false
                  :selected-account false
                  :selected-coin false
                  :active-tab "Send"
                  :reciepent-address false
                  :keys-opened false}))
