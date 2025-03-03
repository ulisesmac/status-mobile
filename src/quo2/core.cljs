(ns quo2.core
  (:refer-clojure :exclude [filter])
  (:require
   quo2.components.avatars.account-avatar
   quo2.components.avatars.channel-avatar
   quo2.components.avatars.group-avatar
   quo2.components.avatars.icon-avatar
   quo2.components.avatars.user-avatar
   quo2.components.avatars.wallet-user-avatar
   quo2.components.banners.banner.view
   quo2.components.buttons.button
   quo2.components.buttons.dynamic-button
   quo2.components.community.community-card-view
   quo2.components.community.community-list-view
   quo2.components.community.community-view
   quo2.components.community.discover-card
   quo2.components.community.icon
   quo2.components.community.token-gating
   quo2.components.counter.counter
   quo2.components.dividers.date
   quo2.components.dividers.divider-label
   quo2.components.dividers.new-messages
   quo2.components.drawers.action-drawers
   quo2.components.dropdowns.dropdown
   quo2.components.header
   quo2.components.icon
   quo2.components.info.info-message
   quo2.components.info.information-box
   quo2.components.list-items.channel
   quo2.components.list-items.menu-item
   quo2.components.list-items.preview-list
   quo2.components.loaders.skeleton
   quo2.components.markdown.text
   quo2.components.messages.author.view
   quo2.components.messages.gap
   quo2.components.messages.system-message
   quo2.components.navigation.floating-shell-button
   quo2.components.navigation.page-nav
   quo2.components.notifications.activity-log.view
   quo2.components.notifications.count-down-circle
   quo2.components.notifications.info-count
   quo2.components.notifications.notification-dot
   quo2.components.notifications.toast
   quo2.components.reactions.reaction
   quo2.components.selectors.disclaimer.view
   quo2.components.selectors.filter.view
   quo2.components.selectors.selectors
   quo2.components.separator
   quo2.components.settings.accounts.view
   quo2.components.settings.privacy-option
   quo2.components.tabs.account-selector
   quo2.components.tabs.tabs
   quo2.components.tags.context-tags
   quo2.components.tags.status-tags
   quo2.components.profile.profile-card.view
   quo2.components.tags.tags))

(def toast quo2.components.notifications.toast/toast)
(def button quo2.components.buttons.button/button)
(def dynamic-button quo2.components.buttons.dynamic-button/dynamic-button)
(def text quo2.components.markdown.text/text)
(def icon quo2.components.icon/icon)
(def separator quo2.components.separator/separator)
(def counter quo2.components.counter.counter/counter)
(def header quo2.components.header/header)
(def action-drawer quo2.components.drawers.action-drawers/action-drawer)
(def dropdown quo2.components.dropdowns.dropdown/dropdown)
(def info-message quo2.components.info.info-message/info-message)
(def information-box quo2.components.info.information-box/information-box)
(def gap quo2.components.messages.gap/gap)
(def system-message quo2.components.messages.system-message/system-message)
(def reaction quo2.components.reactions.reaction/reaction)
(def add-reaction quo2.components.reactions.reaction/add-reaction)
(def tags quo2.components.tags.tags/tags)
(def user-avatar-tag quo2.components.tags.context-tags/user-avatar-tag)
(def context-tag quo2.components.tags.context-tags/context-tag)
(def group-avatar-tag quo2.components.tags.context-tags/group-avatar-tag)
(def audio-tag quo2.components.tags.context-tags/audio-tag)
(def community-tag quo2.components.tags.context-tags/community-tag)
(def tabs quo2.components.tabs.tabs/tabs)
(def account-selector quo2.components.tabs.account-selector/account-selector)
(def floating-shell-button quo2.components.navigation.floating-shell-button/floating-shell-button)
(def status-tag quo2.components.tags.status-tags/status-tag)
(def page-nav quo2.components.navigation.page-nav/page-nav)
(def disclaimer quo2.components.selectors.disclaimer.view/view)
(def checkbox quo2.components.selectors.selectors/checkbox)
(def filter quo2.components.selectors.filter.view/view)
(def skeleton quo2.components.loaders.skeleton/skeleton)
(def author quo2.components.messages.author.view/author)

;;;; AVATAR
(def account-avatar quo2.components.avatars.account-avatar/account-avatar)
(def channel-avatar quo2.components.avatars.channel-avatar/channel-avatar)
(def group-avatar quo2.components.avatars.group-avatar/group-avatar)
(def icon-avatar quo2.components.avatars.icon-avatar/icon-avatar)
(def user-avatar quo2.components.avatars.user-avatar/user-avatar)
(def wallet-user-avatar quo2.components.avatars.wallet-user-avatar/wallet-user-avatar)

;;;; BANNER
(def banner quo2.components.banners.banner.view/banner)

;;;; COMMUNITY
(def community-card-view-item quo2.components.community.community-card-view/community-card-view-item)
(def communities-list-view-item quo2.components.community.community-list-view/communities-list-view-item)
(def communities-membership-list-item
  quo2.components.community.community-list-view/communities-membership-list-item)
(def community-stats-column quo2.components.community.community-view/community-stats-column)
(def community-stats quo2.components.community.community-view/community-stats)
(def community-tags quo2.components.community.community-view/community-tags)
(def community-title quo2.components.community.community-view/community-title)
(def permission-tag-container quo2.components.community.community-view/permission-tag-container)
(def discover-card quo2.components.community.discover-card/discover-card)
(def token-gating quo2.components.community.token-gating/token-gating)
(def community-icon quo2.components.community.icon/community-icon)

;;;; DIVIDERS
(def divider-label quo2.components.dividers.divider-label/divider-label)
(def new-messages quo2.components.dividers.new-messages/new-messages)
(def divider-date quo2.components.dividers.date/date)

;;;; LIST ITEMS
(def channel-list-item quo2.components.list-items.channel/list-item)
(def menu-item quo2.components.list-items.menu-item/menu-item)
(def preview-list quo2.components.list-items.preview-list/preview-list)

;;;; NOTIFICATIONS
(def activity-log quo2.components.notifications.activity-log.view/view)
(def info-count quo2.components.notifications.info-count/info-count)
(def notification-dot quo2.components.notifications.notification-dot/notification-dot)
(def count-down-circle quo2.components.notifications.count-down-circle/circle-timer)

;;;; PROFILE
(def profile-card quo2.components.profile.profile-card.view/profile-card)

;;;; SETTINGS
(def privacy-option quo2.components.settings.privacy-option/card)
(def account quo2.components.settings.accounts.view/account)
