(ns status-im.ui.screens.activity-center.notification.ad-hoc-group-chat.view
  (:require [quo.components.animated.pressable :as animation]
            [quo2.core :as quo2]
            [status-im.i18n.i18n :as i18n]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.screens.activity-center.notification.ad-hoc-group-chat.style :as style]
            [status-im.ui.screens.activity-center.utils :as activity-center.utils]
            [status-im.utils.datetime :as datetime]
            [utils.re-frame :as rf]))

(defn view
  [{:keys [author chat-id chat-name] :as notification}]
  (let [contact   (rf/sub [:contacts/contact-by-identity author])
        ;; TODO: Get the notification information (whether user is removed or added) from Status-Go
        removed-user-from-group-chat? true
        title (if removed-user-from-group-chat? (i18n/label :t/removed-from-group-chat) (i18n/label :t/added-to-group-chat))
        context-tag-text (if removed-user-from-group-chat? (i18n/label :t/removed-you-from) (i18n/label :t/added-you-to))
        pressable (if removed-user-from-group-chat?
                    [:<>]
                    [animation/pressable {:on-press (fn []
                                                      (rf/dispatch [:hide-popover])
                                                      (rf/dispatch [:chat.ui/navigate-to-chat chat-id]))}])]
    (conj pressable
          [quo2/activity-log
           {:title     title
            :icon      :main-icons2/placeholder
            :timestamp (datetime/timestamp->relative (:timestamp notification))
            :unread?   (not (:read notification))
            :context   [[quo2/user-avatar-tag
                         {:color          :purple
                          :override-theme :dark
                          :size           :small
                          :style          style/tag
                          :text-style     style/tag-text}
                         (activity-center.utils/contact-name contact)
                         (multiaccounts/displayed-photo contact)]
                        [quo2/text {:style style/tag-text} context-tag-text]
                        [quo2/group-avatar-tag chat-name {:size           :small
                                                          :override-theme :dark
                                                          :color          :purple
                                                          :style          style/tag
                                                          :text-style     style/tag-text}]]}])))
