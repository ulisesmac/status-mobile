(ns quo2.screens.top-nav
  (:require [quo.react-native :as rn]
            [re-frame.core :as re-frame]
            [quo.previews.preview :as preview]
            [status-im.ui.screens.chat.photos :as photos]
            [quo2.components.top-nav :as top-nav]
            [status-im.i18n.i18n :as i18n]
            [quo2.foundations.colors :as colors]
            [status-im.ui.screens.home.styles :as styles]
            [quo2.components.button  :as quo2.button]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.qr-scanner.core :as qr-scanner]
            [reagent.core :as reagent])
  (:require-macros [status-im.utils.views :as views]))

(def descriptor [{:label   "Size:"
                  :key     :size
                  :type    :select
                  :options [{:key   32
                             :value "32"}
                            {:key   24
                             :value "24"}]}])

(defn qr-scanner []
  [quo2.button/button
   {:icon                true
    :size                32
    :type                :grey
    :style               {:margin-left 10}
    :accessibility-label :scan-qr-code-button
    :on-press #(re-frame/dispatch [::qr-scanner/scan-code
                                   {:title   (i18n/label :t/add-bootnode)
                                    :handler :bootnodes.callback/qr-code-scanned}])}
   :main-icons2/scanner])

(defn qr-code []
  [quo2.button/button
   {:icon                true
    :type                :grey
    :size                32
    :style               {:margin-left 10}
    :accessibility-label :contact-qr-code-button}
   :main-icons2/qr-code])

(views/defview notifications-button []
  (views/letsubs [notif-count [:activity.center/notifications-count]]
                 [rn/view
                  [quo2.button/button {:icon                true
                                       :type                :grey
                                       :size                32
                                       :style               {:margin-left 10}
                                       :accessibility-label "notifications-button"
                                       :on-press #(do
                                                    (re-frame/dispatch [:mark-all-activity-center-notifications-as-read])
                                                    (re-frame/dispatch [:navigate-to :notifications-center]))}
                   :main-icons2/notifications]
                  (when (pos? notif-count)
                    [rn/view {:style (merge (styles/counter-public-container) {:top 5 :right 5})
                              :pointer-events :none}
                     [rn/view {:style               styles/counter-public
                               :accessibility-label :notifications-unread-badge}]])]))

(defn cool-preview []
  (let [account @(re-frame/subscribe [:multiaccount])]
    (fn []
      [rn/view {:padding-vertical 60}
       [top-nav/topbar
        {:navigation      :none
         :left-component  [rn/view {:margin-left 20}
                           [photos/photo (multiaccounts/displayed-photo account)
                            {:size 32}]]
         :right-component [rn/view {:flex-direction :row
                                    :margin-right 20}
                           [qr-scanner]
                           [qr-code]
                           [notifications-button]]}]])))

(defn preview-top-nav []
  [rn/view {:background-color (colors/theme-colors
                               colors/neutral-5
                               colors/neutral-95)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])