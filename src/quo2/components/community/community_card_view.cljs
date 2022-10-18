(ns quo2.components.community.community-card-view
  (:require
   [quo2.components.community.community-view :as community-view]
   [status-im.utils.handlers :refer [<sub]]
   [status-im.ui.components.react :as react]
   [status-im.ui.screens.communities.styles :as styles]
   [status-im.ui.screens.communities.icon :as communities.icon]))

(defn community-card-view-item
  [{:keys [name description locked
           status tokens cover tags] :as community} on-press]
  (let [width (<sub [:dimensions/window-width])]
    [react/view {:width width
                 :margin-bottom      16
                 :flex               1
                 :padding-horizontal 20}
     [react/touchable-opacity {:on-press on-press}
      [react/view {:style (styles/community-card 20)}
       [react/view {:style         {:height          230
                                    :border-radius   20}
                    :on-press      on-press}
        [react/view
         {:flex 1}
         [react/view (styles/community-cover-container 40)
          [react/image
           {:source cover
            :style
            {:flex 1
             :border-radius 20}}]]
         [react/view (styles/card-view-content-container 12)
          [react/view (styles/card-view-chat-icon 48)
           [communities.icon/community-icon-redesign community 48]]
          (when (= status :gated)
            [react/view (styles/permission-tag-styles)
             [community-view/permission-tag-container {:locked locked
                                                       :status status
                                                       :tokens tokens}]])
          [community-view/community-title
           {:title name
            :description description}]
          [community-view/community-stats-column :card-view]
          [community-view/community-tags tags]]]]]]]))

