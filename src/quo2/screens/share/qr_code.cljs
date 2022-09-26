(ns quo2.screens.share.qr-code
  (:require [reagent.core :as reagent]
            [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [quo2.foundations.colors :as colors]
            [quo2.components.share.qr-code :as quo2]))

(def descriptor  [{:label   "Type"
                   :key     :type
                   :type    :select
                   :options [{:key   :profile
                              :value "Profile"}
                             {:key   :wallet-single-chain
                              :value "Wallet Single Chain"}
                             {:key   :wallet-multi-chain
                              :value "Wallet Multi Chain"}]}
                  {:label "link to QR"
                   :key   :qr-link
                   :type  :text}
                  {:label "Network String"
                   :key   :network-string
                   :type  :text}])

(defn cool-preview []
  (let [state    (reagent/atom {:type           :profile
                                :qr-link        "status.app/u/zQ34e2ahd1835eqacc17f6asas12adjie8"
                                :network-string "eth:arb:her:opt:zks"})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view {:padding-vertical 60
                  :align-items      :center}
         [quo2/qr-code @state]]]])))

(defn preview-this []
  [rn/view {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
