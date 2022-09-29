(ns status-im.ui.screens.communities.communities-home
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [quo2.components.community.community-list-view :as community-list-view]
            [quo.components.safe-area :as safe-area]
            [quo2.components.markdown.text :as text]
            [quo2.components.tabs.tabs :as tabs]
            [quo2.foundations.colors :as colors]
            [quo2.components.community.discover-card :as discover-card]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.components.topnav :as topnav]
            [status-im.utils.handlers :refer [<sub >evt]]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.plus-button :as components.plus-button]))

(def selected-tab (reagent/atom :all))

(defn plus-button []
  (let [logging-in? (<sub [:multiaccounts/login])]
    [components.plus-button/plus-button
     {:on-press (when-not logging-in?
                  #(re-frame/dispatch [:bottom-sheet/show-sheet :add-new {}]))
      :loading logging-in?
      :accessibility-label :new-chat-button}]))

(defn render-fn [community-item]
  [community-list-view/communities-membership-list-item community-item])

(defn community-list-key-fn [item]
  (:id item))

(defn get-item-layout-js [_ index]
  #js {:length 64 :offset (* 64 index) :index index})

(defn home-community-segments []
  [react/view {:flex-direction     :row
               :align-items        :center
               :padding-bottom     12
               :padding-top        16
               :height             60
               :margin-bottom      12
               :padding-horizontal 20}
   [react/view {:flex   1}
    [tabs/tabs {:size              32
                :on-change         #(reset! selected-tab %)
                :default-active    :all
                :data [{:id :all   :label (i18n/label :chats/joined)}
                       {:id :open  :label (i18n/label :t/pending)}
                       {:id :gated :label (i18n/label :t/opened)}]}]]])

(defn communities-list [communities]
  [list/flat-list
   {:key-fn                            community-list-key-fn
    :getItemLayout                     get-item-layout-js
    :keyboard-should-persist-taps      :always
    :shows-horizontal-scroll-indicator false
    :data                              communities
    :render-fn                         render-fn}])

(defn segments-community-lists [communities]
  (let [tab @selected-tab]
    [react/view {:padding-left 20}
     (case tab
       :all
       [communities-list communities]

       :open
       [communities-list communities]

       :gated
       [communities-list communities])]))

(defn title-column []
  [react/view
   {:flex-direction     :row
    :align-items        :center
    :height             56
    :padding-vertical   12
    :padding-horizontal 20}
   [react/view
    {:flex           1}
    [text/text {:accessibility-label :communities-screen-title
                :margin-right        6
                :weight              :semi-bold
                :size                :heading-1}
     (i18n/label :t/communities)]]
   [plus-button]])

(defn discover-card [joined]
  [react/touchable-without-feedback
   {:on-press           #(>evt [:navigate-to  :discover-communities])}
   [discover-card/discover-card {:joined      joined
                                 :title       (i18n/label :t/discover)
                                 :description (i18n/label :t/whats-trending)}]])

(defn communities-home []
  (let [multiaccount (<sub [:multiaccount])
        communities  (<sub [:communities/communities])]
    [react/view {:style {:flex             1}}
     [topbar/topbar
      {:navigation      :none
       :left-component  [react/view {:margin-left 12}
                         [photos/photo (multiaccounts/displayed-photo multiaccount)
                          {:size 32}]]
       :right-component [react/view {:flex-direction :row
                                     :margin-right 12}
                         [topnav/search]
                         [topnav/qr-scanner]
                         [topnav/qr-code]
                         [topnav/notifications-button]]
       :new-ui?         true
       :border-bottom   false}]
     [title-column]
     [react/scroll-view
      [discover-card false]
      [home-community-segments]
      [segments-community-lists communities]]]))

(defn views []
  [safe-area/consumer
   (fn [insets]
     [react/view {:style {:flex             1
                          :padding-top      (:top insets)
                          :background-color (colors/theme-colors
                                             colors/neutral-5
                                             colors/neutral-95)}}
      [communities-home]])])


