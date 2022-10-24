(ns status-im.ui.screens.communities.sort-communities-sheet
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [quo2.components.list-items.menu-item :as menu-item]
            [status-im.ui.components.react :as react]
            [status-im.i18n.i18n :as i18n]))

(def sort-by-selected (reagent/atom :name))

(defn hide-sheet-and-reset-sort-by [selected]
  (reset! sort-by-selected selected)
  (re-frame/dispatch [:bottom-sheet/hide]))

(defn sort-communities-view []
  [:<>
   [react/view {:margin-left    20
                :padding-bottom 12}
    [text/text
     {:style {:accessibility-label :sort-communities-title
              :color               (colors/theme-colors
                                    colors/neutral-50
                                    colors/neutral-40)
              :weight              :medium
              :size                :paragraph-2}}
     (i18n/label :t/sort-communities)]]
   [menu-item/menu-item
    {:type               :main
     :title               (i18n/label :t/alphabetically)
     :accessibility-label :alphabetically
     :icon                :main-icons2/alphabetically
     :on-press            #(hide-sheet-and-reset-sort-by :name)}]
   [menu-item/menu-item
    {:type               :main
     :title               (i18n/label :t/total-members)
     :accessibility-label :total-members
     :icon                :main-icons2/members
     :on-press            #(hide-sheet-and-reset-sort-by :total-members)}]
   [menu-item/menu-item
    {:type                :main
     :title               (i18n/label :t/active-members)
     :accessibility-label :active-members
     :icon                :main-icons2/active-member
     :on-press            #(hide-sheet-and-reset-sort-by :active-members)}]
   [menu-item/menu-item
    {:type               :main
     :title               (i18n/label :t/mutal-contacts)
     :accessibility-label :mutual-contacts
     :icon                :main-icons2/friend
     :on-press            #(hide-sheet-and-reset-sort-by :mutual-contacts)}]])

(def sort-communities-sheet
  {:content sort-communities-view})
