(ns quo2.components.tabs.tabs
  (:require [quo.react-native :as rn]
            [quo2.components.tabs.tab :as tab]
            [reagent.core :as reagent]
            [quo2.foundations.colors :as colors]
            [quo2.components.notifications.notification-dot :refer [notification-dot]]))

(def default-tab-size 32)

(defn indicator []
  [rn/view {:position         :absolute
            :z-index          1
            :right            -2
            :top              -2
            :width            10
            :height           10
            :border-radius    5
            :justify-content  :center
            :align-items      :center
            :background-color (colors/theme-colors colors/neutral-5 colors/neutral-95)}
   [notification-dot]])

(defn tabs [{:keys [default-active on-change style]}]
  (let [active-tab-id (reagent/atom default-active)]
    (fn [{:keys [data size] :or {size default-tab-size}}]
      [rn/view (merge {:flex-direction :row} style)
       (doall
        (for [{:keys [label id notification-dot? accessibility-label]} data]
          ^{:key id}
          [rn/view {:style {:margin-right (if (= size default-tab-size) 12 8)}}
           (when notification-dot?
             [indicator])
           [tab/tab
            {:id                  id
             :size                size
             :accessibility-label accessibility-label
             :active              (= id @active-tab-id)
             :on-press            (fn []
                                    (reset! active-tab-id id)
                                    (when on-change
                                      (on-change id)))}
            label]]))])))
