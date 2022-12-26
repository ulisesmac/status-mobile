(ns quo2.components.banners.banner.view
  (:require [quo2.components.banners.banner.style :as style]
            [quo2.components.counter.counter :as counter]
            [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]))

(defn banner
  [{:keys [show-pin? latest-pin-text pins-count on-press]}]
  (when (pos? pins-count)
    [rn/touchable-opacity
     {:accessibility-label :pinned-banner
      :style               style/container
      :active-opacity      1
      :on-press            on-press}
     (when show-pin?
       [icons/icon :i/pin
        {:color (colors/theme-colors colors/neutral-100 colors/white)
         :size  20}])
     [text/text
      {:number-of-lines 1
       :size            :paragraph-2
       :style           style/text}
      latest-pin-text]
     [rn/view
      {:accessibility-label :pins-count
       :style               style/counter}
      (when (> pins-count 1) [counter/counter {:type :secondary} pins-count])]]))