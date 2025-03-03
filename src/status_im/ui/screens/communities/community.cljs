(ns status-im.ui.screens.communities.community
  (:require
   [utils.i18n :as i18n]
   [quo.core :as quo]
   [quo.design-system.colors :as colors]
   [quo2.components.community.style :as styles] ;; TODO reimplement with quo2 library and new
                                                ;; designs
   [quo2.components.navigation.floating-shell-button :as floating-shell-button]
   [react-native.core :as rn]
   [status-im.chat.models.link-preview :as link-preview]
   [status-im.communities.core :as communities]
   [status-im2.constants :as constants]
   [status-im.react-native.resources :as resources]
   [status-im.ui.components.accordion :as accordion]
   [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
   [status-im.ui.components.icons.icons :as icons]
   [status-im.ui.components.list.views :as list]
   [status-im.ui.components.plus-button :as components.plus-button]
   [status-im.ui.components.react :as react]
   [status-im.ui.components.toolbar :as toolbar]
   [status-im.ui.components.topbar :as topbar]
   [status-im.ui.screens.chat.photos :as photos]
   [status-im.ui.screens.chat.sheets :as sheets]
   [status-im.ui.screens.home.views.inner-item :as inner-item]
   [status-im.utils.core :as utils]
   [utils.datetime :as datetime]
   [utils.re-frame :as rf]))

(def request-cooldown-ms (* 24 60 60 1000))

(defn can-request-access-again?
  [requested-at]
  (> (datetime/timestamp) (+ (* requested-at 1000) request-cooldown-ms)))

(defn toolbar-content
  [id display-name color images show-members-count? members]
  (let [thumbnail-image (get-in images [:thumbnail :uri])]
    [rn/view
     {:style {:flex           1
              :align-items    :center
              :flex-direction :row}}
     [rn/view {:padding-right 10}
      (cond
        (= id constants/status-community-id)
        [rn/image
         {:source (resources/get-image :status-logo)
          :style  {:width  40
                   :height 40}}]
        (seq thumbnail-image)
        [photos/photo thumbnail-image {:size 40}]

        :else
        [chat-icon.screen/chat-icon-view-toolbar
         id
         true
         display-name
         (or color (rand-nth colors/chat-colors)) nil 36])]
     [rn/view {:style {:flex 1 :justify-content :center}}
      [quo/text
       {:number-of-lines     1
        :accessibility-label :community-name-text}
       display-name]
      [quo/text
       {:number-of-lines 1
        :size            :small
        :color           :secondary}
       (if show-members-count?
         (i18n/label-pluralize members :t/community-members {:count members})
         (i18n/label :t/open-membership))]]]))

(defn hide-sheet-and-dispatch
  [event]
  (rf/dispatch [:bottom-sheet/hide])
  (rf/dispatch event))

(defn community-plus-actions
  [{:keys [id permissions can-manage-users?]}]
  (let [can-invite? (and can-manage-users?
                         (not= (:access permissions) constants/community-no-membership-access))
        can-share?  (not= (:access permissions) constants/community-invitation-only-access)]
    [:<>
     [quo/list-item
      {:theme               :accent
       :title               (i18n/label :t/create-channel)
       :accessibility-label :community-create-channel
       :icon                :main-icons/channel
       :on-press            #(hide-sheet-and-dispatch [::communities/create-channel-pressed id])}]
     [quo/list-item
      {:theme               :accent
       :title               (i18n/label :t/create-category)
       :accessibility-label :community-create-category
       :icon                :main-icons/channel-category
       :on-press            #(hide-sheet-and-dispatch [:open-modal :create-community-category
                                                       {:community-id id}])}]
     [quo/separator]
     (when can-invite?
       [quo/list-item
        {:theme               :accent
         :title               (i18n/label :t/invite-people)
         :icon                :main-icons/share
         :accessibility-label :community-invite-people
         :on-press            #(rf/dispatch [:communities/invite-people-pressed id])}])
     (when (and can-share? (not can-invite?))
       [quo/list-item
        {:theme               :accent
         :title               (i18n/label :t/invite-people)
         :icon                :main-icons/share
         :accessibility-label :community-share
         :on-press            #(rf/dispatch [:communities/share-community-pressed id])}])]))

(defn community-actions
  [{:keys [id name images color can-manage-users?]}]
  (let [thumbnail-image (get-in images [:thumbnail :uri])]
    [:<>
     [quo/list-item
      {:title    name
       :subtitle (i18n/label :t/community-info)
       :on-press #(hide-sheet-and-dispatch [:navigate-to :community-management {:community-id id}])
       :chevron  true
       :icon     (cond
                   (= id constants/status-community-id)
                   [rn/image
                    {:source (resources/get-image :status-logo)
                     :style  {:width  40
                              :height 40}}]
                   (seq thumbnail-image)
                   [photos/photo thumbnail-image {:size 40}]

                   :else
                   [chat-icon.screen/chat-icon-view-chat-sheet
                    name
                    true
                    name
                    (or color (rand-nth colors/chat-colors))])}]
     [quo/list-item
      {:theme               :accent
       :title               (i18n/label :t/mark-all-read)
       :accessibility-label :mark-all-read-button
       :icon                :main-icons/check
       :on-press            #(hide-sheet-and-dispatch [:chat.ui/mark-all-read-in-community-pressed id])}]
     (when can-manage-users?
       [:<>
        [quo/list-item
         {:theme               :accent
          :title               (i18n/label :t/export-key)
          :accessibility-label :community-export-key
          :icon                :main-icons/objects
          :on-press            #(hide-sheet-and-dispatch [::communities/export-pressed id])}]
        [quo/separator]
        [quo/list-item
         {:theme               :accent
          :title               (i18n/label :t/edit-chats)
          :accessibility-label :community-edit-chats
          :icon                :main-icons/edit
          :on-press            #(hide-sheet-and-dispatch
                                 [:open-modal :community-reorder-categories {:community-id id}])}]])]))

(defn blank-page
  [text]
  [rn/view
   {:style {:padding 16 :flex 1 :flex-direction :row :align-items :center :justify-content :center}}
   [quo/text
    {:align :center
     :color :secondary}
    text]])

(defn community-chat-item
  [{:keys [chat-id] :as home-item} _ _ _]
  [inner-item/home-list-item-old
   ;; We want communities to behave as public chats when it comes to
   ;; unread indicator
   (assoc home-item :public? true)
   {:on-press      (fn []
                     (rf/dispatch [:dismiss-keyboard])
                     (rf/dispatch [:chat.ui/navigate-to-chat-nav2 chat-id])
                     (rf/dispatch [:search/home-filter-changed nil]))
    :on-long-press #(rf/dispatch [:bottom-sheet/show-sheet
                                  {:content (fn []
                                              [sheets/actions home-item])}])}])

(defn categories-accordion
  [community-id chats categories data]
  [:<>
   (for [{:keys [name id state]} categories]
     ^{:key (str "cat" name id)}
     [:<>
      [accordion/section
       {:on-open  #(rf/dispatch [::communities/store-category-state community-id id true])
        :on-close #(rf/dispatch [::communities/store-category-state community-id id false])
        :default  state
        :title    [rn/view styles/category-item
                   [icons/icon :main-icons/channel-category {:color colors/gray}]
                   [rn/text {:style {:font-size 17 :margin-left 10 :color colors/black}} name]]
        :content  [:<>
                   (for [chat (get chats id)]
                     ^{:key (str "chat" chat id)}
                     [community-chat-item chat nil nil data])]}]
      [quo/separator]])])

(defn community-chat-list
  [community-id categories from-chat]
  (let [chats (rf/sub [:chats/sorted-categories-by-community-id community-id])]
    (if (and (empty? categories) (empty? chats))
      [blank-page (i18n/label :t/welcome-community-blank-message)]
      [list/flat-list
       {:key-fn                       :chat-id
        :content-container-style      {:padding-bottom 8}
        :keyboard-should-persist-taps :always
        :data                         (get chats "")
        :render-data                  {:from-chat from-chat}
        :render-fn                    community-chat-item
        :header                       [categories-accordion community-id chats categories
                                       {:from-chat from-chat}]
        :footer                       [rn/view {:height 68}]}])))

(defn channel-preview-item
  [{:keys [id name]}]
  (let [color colors/default-community-color]
    [quo/list-item
     {:icon                      [chat-icon.screen/chat-icon-view-chat-list
                                  id true name color false false]
      :title                     [rn/view
                                  {:flex-direction :row
                                   :flex           1
                                   :padding-right  16
                                   :align-items    :center}
                                  [icons/icon :main-icons/tiny-group
                                   {:color           colors/black
                                    :width           15
                                    :height          15
                                    :container-style {:width        15
                                                      :height       15
                                                      :margin-right 2}}]
                                  [quo/text
                                   {:weight              :medium
                                    :accessibility-label :chat-name-text
                                    :ellipsize-mode      :tail
                                    :number-of-lines     1}
                                   (utils/truncate-str name 30)]]
      :title-accessibility-label :chat-name-text}]))

(defn community-channel-preview-list
  [_ chats-without-id]
  (let [chats (reduce-kv
               (fn [acc k v]
                 (conj acc (assoc v :id (name k))))
               []
               chats-without-id)]
    [list/flat-list
     {:key-fn                       :id
      :content-container-style      {:padding-vertical 8}
      :keyboard-should-persist-taps :always
      :data                         chats
      :render-fn                    channel-preview-item}]))

(defn unknown-community
  [community-id]
  (let [fetching (rf/sub [:communities/fetching-community community-id])]
    [:<> {:style {:flex 1}}
     [topbar/topbar {:title (if fetching (i18n/label :t/fetching-community) (i18n/label :t/not-found))}]
     [rn/view
      {:style {:padding 16 :flex 1 :flex-direction :row :align-items :center :justify-content :center}}

      [quo/button
       {:on-press (when-not fetching #(rf/dispatch [::link-preview/resolve-community-info community-id]))
        :disabled fetching
        :color    :secondary}
       (if fetching
         [react/small-loading-indicator]
         (i18n/label :t/fetch-community))]]]))

(defn community
  []
  (let [{:keys [community-id from-chat]} (rf/sub [:get-screen-params :community])]
    (fn []
      (let [{:keys [id chats name images members permissions color joined
                    can-request-access? can-join? requested-to-join-at admin]
             :as   community}
            (rf/sub [:communities/community community-id])
            categories (rf/sub [:communities/sorted-categories community-id])]
        (if community
          [rn/view {:style {:flex 1}}
           [topbar/topbar
            {:content
             [toolbar-content
              id
              name
              color
              images
              (not= (:access permissions) constants/community-no-membership-access)
              (count members)]
             :right-accessories
             (when (or admin joined)
               [{:icon                :main-icons/more
                 :accessibility-label :community-menu-button
                 :on-press            #(rf/dispatch [:bottom-sheet/show-sheet
                                                     {:content (fn []
                                                                 [community-actions community])}])}])}]
           (if joined
             [community-chat-list id categories false from-chat]
             [community-channel-preview-list id chats])
           (when admin
             [components.plus-button/plus-button-old
              {:on-press            #(rf/dispatch [:bottom-sheet/show-sheet
                                                   {:content (fn []
                                                               [community-plus-actions community])}])
               :accessibility-label :new-chat-button}])
           (when-not joined
             (cond
               can-join?
               [toolbar/toolbar
                {:show-border? true
                 :center       [quo/button
                                {:on-press #(rf/dispatch [:communities/join id])
                                 :type     :secondary}
                                (i18n/label :t/join)]}]
               can-request-access?
               (if (and (pos? requested-to-join-at)
                        (not (can-request-access-again? requested-to-join-at)))
                 [toolbar/toolbar
                  {:show-border? true
                   :left         [quo/text {:color :secondary}
                                  (i18n/label :t/membership-request-pending)]}]
                 [toolbar/toolbar
                  {:show-border? true
                   :center       [quo/button
                                  {:on-press #(rf/dispatch [::communities/request-to-join id])
                                   :type     :secondary}
                                  (i18n/label :t/request-access)]}])
               :else
               [toolbar/toolbar
                {:show-border? true
                 :center       [quo/button
                                {:on-press #(rf/dispatch [:communities/join id])
                                 :type     :secondary}
                                (i18n/label :t/follow)]}]))
           [floating-shell-button/floating-shell-button
            {:jump-to {:on-press #(rf/dispatch [:shell/navigate-to-jump-to])
                       :label    (i18n/label :t/jump-to)}}
            {:position :absolute
             :bottom   70}]]
          [unknown-community community-id])))))
