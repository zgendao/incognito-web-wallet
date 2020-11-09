(ns app.pin
  (:require [reagent.core :refer [atom]]
            [crypto-js :as CryptoJS]
            [app.icons :refer [loader alert-icon refresh-icon]]
            [app.storage :refer [state local]]
            [app.tabs :refer [input]]
            [app.api :refer [login-init]]
            [async-await.core :refer [async await]]))

(defn decrypt []
  (try
    (let [decoded (.toString
                    (.decrypt (.-AES CryptoJS) (:pw @local) (get-in @state [:login :pin]))
                    (.. CryptoJS -enc -Utf8))]
      (if (= 12 (count (clojure.string/split decoded #" ")))
        (do
          (swap! state assoc :pw decoded)
          (login-init))
        (throw (js/Error. "Pw blank"))))
    (catch :default e
      (print e)
      (swap! state assoc-in [:login :error] "Wrong passcode. Try again!"))))

(defn pin-input [label submit-fn]
  (let [pin (atom ["" "" "" ""])]
    (fn []
      [:<>
        [:h5 label]
        [:div.pin-wrapper
          (into [:<>]
            (for [x (range 4)]
              [:input {:id (str "pin_" x) :name (str "pin_" x) :key (str "pin_" x)
                        :type "password" :class "pin-input"
                        :autoFocus (when (= x 0) true)
                        :pattern "[0-9]*" :inputMode "numeric"
                        :value (get @pin x) :maxLength 1
                        :on-change
                          (fn [e]
                            (swap! pin assoc x (-> e .-target .-value))
                            (swap! state assoc-in [:login :error] nil)
                            (when (not-any? #(= "" %) @pin)
                              (submit-fn (reduce str @pin))))
                        :on-key-down
                          (fn [e]
                            (if (or (= e.key "Backspace") (= e.key "ArrowLeft"))
                              (when (> x 0)
                                (js/setTimeout #(.select (.getElementById js/document (str "pin_" (dec x)))) 10))
                              (when (< x 3)
                                (js/setTimeout #(.select (.getElementById js/document (str "pin_" (inc x)))) 10))))}]))]
        [:p.input__error.inline-icon {:class (when (get-in @state [:login :error]) "show")}
          (when (get-in @state [:login :error])
            [:<> [alert-icon "var(--color-alert)"] (get-in @state [:login :error])])]])))

(defn onboard-screen []
  (let [confirm-state? (atom false)]
    (fn []
      [:div.welcome-screen
        [:h1.welcome-title "Welcome to Incognito Web Wallet!"]
        [:div
          ^{:key (str "pin-input" @confirm-state?)}
          [pin-input (if @confirm-state? "Re-confirm your passcode" "Set your passcode first")
            (fn [pin]
              (if @confirm-state?
                (do
                  (if (= pin (get-in @state [:login :pin]))
                    (login-init)
                    (do
                      (swap! state assoc-in [:login :error] "Passcodes didn't match"))))
                (do
                  (swap! state assoc-in [:login :pin] pin)
                  (reset! confirm-state? true))))]
          [:div.btn-wrapper
            [:button.btn.inline-icon {:class (when (get-in @state [:login :error]) "show")
                                      :on-click (fn []
                                                  (swap! state assoc-in [:login :error] nil)
                                                  (reset! confirm-state? false))}
              [refresh-icon]
              "Reset"]]]])))

(defn login-screen []
  [:div.welcome-screen
    [:h1.welcome-title "Welcome back!"]
    [:div
      [pin-input "Enter your passcode to unlock wallet"
        (fn [pin]
          (swap! state assoc-in [:login :pin] pin)
          (decrypt))]]])

(defn welcome-screen []
  [:div.fullscreen-center
    (if (:pw @state)
      [loader]
      (if (:backupkey @local)
        [login-screen]
        [onboard-screen]))])
