(ns app.storage
  (:require [reagent.core :as reagent :refer [atom]]
            [alandipert.storage-atom :refer [local-storage]]))

(defonce state (atom {:prv-price 0
                      :wasm-loaded false
                      :keys-opened false
                      :selected-account false
                      :selected-coin false
                      :active-tab "Send"
                      :send-data {:reciepent-address nil
                                  :amount nil
                                  :fee nil
                                  :note nil}}))

(defonce accounts (atom [{:name "Account 0"
                          :keys {:incognito "12RxhL3XRDLkqL9pc3Z3sCn2E9y6Sg9dYHZL7W24geukJA9n34G4MeoSmVmBak1CYBx5d3evZNSLvUYh7KWPq3Jwx7c4g8831SGEqdi"
                                 :public "12RxhL3XRDLkqL9pc3Z3sCn2E9y6Sg9dYHZL7W24geukJA9n34G4MeoSmVmBak1CYBx5d3evZNSLvUYh7KWPq3Jwx7c4g8831SGEqdi"
                                 :private "112t8rnXDhcqHHE9nU6wfcSFvYMtCcQGh6bJRp9BMpY9PBNSHZnwee3Po4XF9yFTwQaa9c6gA9sky8DfPzSRjhr23jWjDsEkzxHuiFFaWWuC"
                                 :readonly "112t8rnXDhcqHHE9nU6wfcSFvYMtCcQGh6bJRp9BMpY9PBNSHZnwee3Po4XF9yFTwQaa9c6gA9sky8DfPzSRjhr23jWjDsEkzxHuiFFaWWuC"
                                 :validator "12NsWr27MnVm6Na6yxBhmNqHk6DckBPEL9dn91jX4qLw1jzu8jp"}
                          :coins [{:id 0
                                   :amount 20}
                                  {:id 1
                                   :amount 1}
                                  {:id 2
                                   :amount 2}
                                  {:id 3
                                   :amount 40}
                                  {:id 4
                                   :amount 0}]}
                         {:name "Account 1"
                          :keys {:incognito "asuidgfhO=ZUATZS(=D/87asdHZL7W24geukJA9n34G4MeoSmVmBak1CYBx5d3evZNSLvUYh7KWPq3Jwx7c4g8831SGEqdi"
                                 :public "jh,ngasdbf i7/A%VS)=/D%V78g9dYHZL7W24geukJA9n34G4MeoSmVmBak1CYBx5d3evZNSLvUYh7KWPq3Jwx7c4g8831SGEqdi"
                                 :private "mnbvds 987vA/%Fö87 6v98a7dfCcQGh6bJRp9BMpY9PBNSHZnwee3Po4XF9yFTwQaa9c6gA9sky8DfPzSRjhr23jWjDsEkzxHuiFFaWWuC"
                                 :readonly "sdfghsretz SD fa8sédzgön 98w798QGh6bJRp9BMpY9PBNSHZnwee3Po4XF9yFTwQaa9c6gA9sky8DfPzSRjhr23jWjDsEkzxHuiFFaWWuC"
                                 :validator "168597654AHKSFDKUZGTGUZAHk6DckBPEL9dn91jX4qLw1jzu8jp"}
                          :coins [{:id 0
                                   :amount 18}
                                  {:id 1
                                   :amount 0.0034}
                                  {:id 2
                                   :amount 0.23}
                                  {:id 3
                                   :amount 14}
                                  {:id 4
                                   :amount 0}]}]))

;majd apiról fetchelve és frissen tartva
(defonce coins (atom [{"ID" 0
                        "TokenID" "ffd8d42dc40a8d166ea4848baf8b5f6e912ad79875f4373070b59392b1756c8f" 
                        "Symbol" "PRV" 
                        "Name" "Privacy" 
                        "Default" true 
                        "PriceUsd" 0.96
                        "Verified" true 
                        "PricePrv" 1 
                        "volume24" 12} 
                      {"ID" 1 
                        "CreatedAt" "2019-10-29T03 51 52Z" 
                        "UpdatedAt" "2020-07-30T16 17 32Z" 
                        "DeletedAt" nil 
                        "TokenID" "ffd8d42dc40a8d166ea4848baf8b5f6e912ad79875f4373070b59392b1756c8f" 
                        "Symbol" "ETH" 
                        "OriginalSymbol" "" 
                        "Name" "Ethereum" 
                        "ContractID" "" 
                        "Decimals" 18 
                        "PDecimals" 9 
                        "Status" 1 
                        "Type" 0 
                        "CurrencyType" 1 
                        "PSymbol" "pETH" 
                        "Default" true 
                        "UserID" 1 
                        "PriceUsd" 318.82 
                        "Verified" true 
                        "LiquidityReward" 8.5 
                        "PercentChange1h" "0.21" 
                        "PercentChangePrv1h" "0.01" 
                        "CurrentPrvPool" 236855979645294 
                        "PricePrv" 333.966443693 
                        "volume24" 12} 
                      {"ID" 2 
                        "CreatedAt" "2019-10-29T03 51 52Z" 
                        "UpdatedAt" "2020-07-30T16 17 22Z" 
                        "DeletedAt" nil 
                        "TokenID" "b832e5d3b1f01a4f0623f7fe91d6673461e1f5d37d91fe78c5c2e6183ff39696" 
                        "Symbol" "BTC" 
                        "OriginalSymbol" "" 
                        "Name" "Bitcoin" 
                        "ContractID" "" 
                        "Decimals" 8 
                        "PDecimals" 9 
                        "Status" 1 
                        "Type" 0 
                        "CurrencyType" 2 
                        "PSymbol" "pBTC" 
                        "Default" true 
                        "UserID" 1 
                        "PriceUsd" 11015.06 
                        "Verified" true 
                        "LiquidityReward" 5 
                        "PercentChange1h" "0.23" 
                        "PercentChangePrv1h" "0.48" 
                        "CurrentPrvPool" 856176568166850 
                        "PricePrv" 11417.704431447 
                        "volume24" 0} 
                      {"ID" 3 
                        "CreatedAt" "2019-10-29T03 51 52Z" 
                        "UpdatedAt" "2020-07-30T16 17 23Z" 
                        "DeletedAt" nil 
                        "TokenID" "b2655152784e8639fa19521a7035f331eea1f1e911b2f3200a507ebb4554387b" 
                        "Symbol" "BNB" 
                        "OriginalSymbol" "" 
                        "Name" "Binance Coin" 
                        "ContractID" "" 
                        "Decimals" 8 
                        "PDecimals" 9 
                        "Status" 1 
                        "Type" 0 
                        "CurrencyType" 4 
                        "PSymbol" "pBNB" 
                        "Default" false 
                        "UserID" 1 
                        "PriceUsd" 19.67 
                        "Verified" true 
                        "LiquidityReward" 0 
                        "PercentChange1h" "-0.18" 
                        "PercentChangePrv1h" "0" 
                        "CurrentPrvPool" 39680103218251 
                        "PricePrv" 20.890174924 
                        "volume24" 0} 
                      {"ID" 4 
                        "CreatedAt" "2019-10-29T03 51 52Z" 
                        "UpdatedAt" "2020-07-30T16 17 21Z" 
                        "DeletedAt" nil 
                        "TokenID" "a0a22d131bbfdc892938542f0dbe1a7f2f48e16bc46bf1c5404319335dc1f0df" 
                        "Symbol" "TOMO" 
                        "OriginalSymbol" "" 
                        "Name" "TomoChain" 
                        "ContractID" "" 
                        "Decimals" 18 
                        "PDecimals" 9 
                        "Status" 1 
                        "Type" 0 
                        "CurrencyType" 9 
                        "PSymbol" "pTOMO" 
                        "Default" false 
                        "UserID" 1 
                        "PriceUsd" 0.894986 
                        "Verified" true 
                        "LiquidityReward" 0 
                        "PercentChange1h" "-0.71" 
                        "PercentChangePrv1h" "0" 
                        "CurrentPrvPool" 34199068016 
                        "PricePrv" 1.103414273 
                        "volume24" 0} 
                      {"ID" 5 
                        "CreatedAt" "2019-10-29T03 51 52Z" 
                        "UpdatedAt" "2020-05-26T03 25 07Z" 
                        "DeletedAt" nil 
                        "TokenID" "b20810f4d2a1dde8046028819d9fa12549e04ce14fb299594da8cfca9be5d856" 
                        "Symbol" "USD" 
                        "OriginalSymbol" "" 
                        "Name" "StableCoin" 
                        "ContractID" "" 
                        "Decimals" 0 
                        "PDecimals" 9 
                        "Status" 1 
                        "Type" 0 
                        "CurrencyType" 6 
                        "PSymbol" "pUSD" 
                        "Default" false 
                        "UserID" 1 
                        "PriceUsd" 1 
                        "Verified" false 
                        "LiquidityReward" 0 
                        "PercentChange1h" "" 
                        "PercentChangePrv1h" "" 
                        "CurrentPrvPool" 0 
                        "PricePrv" 0 
                        "volume24" 0}]))


