(ns app.pin
  (:require [crypto-js :as CryptoJS]
            [app.icons :refer [loader]]
            [app.storage :refer [state local]]
            [app.tabs :refer [input show-error]]
            [app.api :refer [login-init]]
            [async-await.core :refer [async await]]))


(defn decrypt []
  (try
      (let [decoded (.toString (.decrypt (.-AES CryptoJS) (:pw @local) (get-in @state [:login :pin])) (.. CryptoJS -enc -Utf8))]
        (if (= 12 (count (clojure.string/split decoded #" ")))
          (do
            (swap! state assoc :pw decoded)
            (login-init))
          (throw (js/Error. "Pw blank"))))
    (catch :default e
      (swap! state assoc :badlogin true))))


(defn login-screen []
  (if (not (:pw @state))
    (if (:backupkey @local)
      [:span.loadingScreen
       [:div.inputWrapper
        [input :login :pin "Please enter your pin:" "password" "pincode"]
        [:button {:on-click #(decrypt)} "Login"]]
       (when (:badlogin @state)
         [:p "Wrong pin!"])]
      [:span.loadingScreen
       [:div.inputWrapper
        [input :login :pin "Please enter a pin to generate your wallet:" "password" "Make sure to remember it!"]
        [:button.btn {:on-click #(login-init) :style {:margin-left "5%"}} "Login"]]])
    [loader]))
