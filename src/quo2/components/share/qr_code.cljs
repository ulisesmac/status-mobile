(ns quo2.components.share.qr-code
  (:require [quo.react-native :as rn]
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [quo2.foundations.colors :as colors]))

(defn qr-code [{:keys [type qr-link network-string]}]
  [:<>
   [rn/text {:style {:font-weight :700 :font-size 22}} "QR code component"]
   [qr-code-viewer/qr-code-view 303 qr-link 12 colors/white]
   ]
  )



