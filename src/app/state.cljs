(ns app.state
  (:require [reagent.core :as reagent :refer [atom]]
            [alandipert.storage-atom :refer [local-storage]]))

(defonce state (atom {:prv-price 0
                      :wasm-loaded false
                      :selected-account false
                      :selected-coin false
                      :active-tab "Send"
                      :send-data {:reciepent-address nil
                                  :amount nil
                                  :fee nil
                                  :note nil}
                      :keys-opened false}))
