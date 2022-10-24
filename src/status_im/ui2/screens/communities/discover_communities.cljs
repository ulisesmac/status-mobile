(ns status-im.ui2.screens.communities.discover-communities
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.list.views :as list]
            [quo.react-native :as rn]
            [quo2.components.separator :as separator]
            [quo2.components.markdown.text :as quo2.text]
            [quo2.components.counter.counter :as quo2.counter]
            [quo2.components.tags.tags :as tags]
            [quo2.components.tags.tag :as tag]
            [quo.components.safe-area :as safe-area]
            [quo2.components.tabs.tabs :as quo2.tabs]
            [status-im.react-native.resources :as resources]
            [status-im.utils.handlers :refer [<sub  >evt]]
            [quo2.components.buttons.button :as quo2.button]
            [status-im.ui.components.search-input.view :as search-input]
            [quo2.components.community.community-card-view :as community-card]
            [quo2.components.community.community-list-view :as community-list]
            [quo2.components.icon :as icons]
            [quo2.foundations.colors :as colors]))

(def selected-tab (reagent/atom :all))
(def view-type   (reagent/atom  :card-view))
(def sort-list-by (reagent/atom :name))
(def expand-tags  (reagent/atom false))
(defonce search-active? (reagent/atom false))

(def mock-community-item-data ;; TODO: remove once communities are loaded with this data.
  {:data {:community-color "#0052FF"
          :status  :gated
          :locked? true
          :cover  (resources/get-image :community-cover)
          :tokens [{:id    1
                    :group [{:id         1
                             :token-icon (resources/get-image :status-logo)}]}]
          :tags   [{:id        1
                    :tag-label (i18n/label :t/music)
                    :resource  (resources/get-image :music)}
                   {:id        2
                    :tag-label (i18n/label :t/lifestyle)
                    :resource  (resources/get-image :lifestyle)}
                   {:id        3
                    :tag-label (i18n/label :t/podcasts)
                    :resource  (resources/get-image :podcasts)}]}})

(defn search-input-wrapper []
  [rn/view {:padding-vertical   10
            :padding-horizontal 4
            :height             52}
   [search-input/search-input
    {:search-active? search-active?
     :before         false
     :placeholder    (i18n/label :t/search-discover-communities)}]])

(defn render-other-fn [community-item]
  (let [item (merge community-item
                    (get mock-community-item-data :data)
                    {:featured       false})]
    (if (= @view-type :card-view)
      [community-card/community-card-view-item item #(>evt [:navigate-to :community-overview item])]
      [community-list/communities-list-view-item item])))

(defn render-featured-fn [community-item]
  (let [item (merge community-item
                    (get mock-community-item-data :data)
                    {:featured       true})]
    [community-card/community-card-view-item item #(>evt [:navigate-to :community-overview item])]))

(defn get-item-layout-js [_ index]
  #js {:length 64 :offset (* 64 index) :index index})

(defn title-and-search []
  [rn/view {:style {:border-bottom-width  1
                    :border-bottom-color  (colors/theme-colors
                                           colors/neutral-10
                                           colors/neutral-80)}}
   [rn/view
    {:height             56
     :padding-vertical   12
     :padding-horizontal 20}
    [quo2.text/text {:accessibility-label :communities-screen-title
                     :weight              :semi-bold
                     :size                :heading-1}
     (i18n/label :t/discover-communities)]]
   [search-input-wrapper]])

(defn expand-and-collapse-tags [tags]
  (if (not @expand-tags)
    [rn/scroll-view {:horizontal                        true
                     :height                            48
                     :margin-right                      40
                     :padding-vertical                  8
                     :align-items                       :center
                     :shows-horizontal-scroll-indicator false
                     :scroll-event-throttle             64}
     [tags/tags {:data          tags
                 :labelled      true
                 :type          :emoji
                 :icon-color    (colors/theme-colors
                                 colors/neutral-50
                                 colors/neutral-40)}]]

    [rn/view {:flex-wrap        :wrap
              :min-height       48
              :align-items      :center
              :padding-vertical 8}
     (for [{:keys [label id resource]} tags]
       ^{:key id}
       [rn/view {:padding-vertical  8
                 :margin-right      8}
        [tag/tag {:id         id
                  :size       32
                  :icon-color (colors/theme-colors
                               colors/neutral-50
                               colors/neutral-40)
                  :type        :emoji
                  :labelled    true
                  :label       label
                  :resource    resource}]])]))

(defn community-filter-tags []
  (let [tags [{:id  1  :label (i18n/label :t/music)     :resource  (resources/get-image :music)}
              {:id  2  :label (i18n/label :t/lifestyle) :resource  (resources/get-image :lifestyle)}
              {:id  3  :label (i18n/label :t/podcasts)  :resource  (resources/get-image :podcasts)}
              {:id  4  :label (i18n/label :t/music)     :resource  (resources/get-image :music)}
              {:id  6  :label (i18n/label :t/lifestyle) :resource  (resources/get-image :lifestyle)}
              {:id  7  :label (i18n/label :t/podcasts)  :resource  (resources/get-image :podcasts)}
              {:id  8  :label (i18n/label :t/music)     :resource  (resources/get-image :music)}
              {:id  9  :label (i18n/label :t/lifestyle) :resource  (resources/get-image :lifestyle)}
              {:id  10 :label (i18n/label :t/podcasts)  :resource  (resources/get-image :podcasts)}]]
    [rn/view
     [rn/view {:flex-direction      :row
               :padding-horizontal  20
               :height              48
               :margin-top          12 
               :align-items         :center}
      (when (not @expand-tags)
        [rn/scroll-view {:horizontal                        true
                         :margin-right                      40
                         :padding-vertical                  8
                         :height                            48
                         :align-items                       :center
                         :shows-horizontal-scroll-indicator false
                         :scroll-event-throttle             64}
         [tags/tags {:data          tags
                     :labelled      true
                     :type          :emoji
                     :icon-color    (colors/theme-colors
                                     colors/neutral-50
                                     colors/neutral-40)}]])
      [rn/view {:style {:margin-left  8
                        :position     :absolute
                        :right        20}}
       [tag/tag {:size      32
                 :icon-color (colors/theme-colors
                              colors/neutral-50
                              colors/neutral-40)
                 :type        :icon
                 :labelled    true
                 :resource   (if @expand-tags
                               :main-icons2/chevron-up
                               :main-icons2/chevron-down)
                 :on-press    #(do (if @expand-tags
                                     (reset! expand-tags false)
                                     (reset! expand-tags true)))}]]]
     (when @expand-tags
       [rn/view {:flex-direction   :row
                 :flex-wrap        :wrap
                 :min-height       48
                 :align-items      :center
                 :padding-left     20
                 :padding-vertical 8}
        (for [{:keys [label id resource]} tags]
          ^{:key id}
          [rn/view {:padding-vertical  8
                    :margin-right      8}
           [tag/tag {:id         id
                     :size       32
                     :icon-color (colors/theme-colors
                                  colors/neutral-50
                                  colors/neutral-40)
                     :type        :emoji
                     :labelled    true
                     :label       label
                     :resource    resource}]])])]))


(defn discover-community-segments []
  [rn/view {:flex               1
            :margin-bottom      8
            :padding-horizontal 20}
   [rn/view {:flex-direction :row
             :padding-top    20
             :padding-bottom 8
             :height         60}
    [rn/view {:flex 1}
     [quo2.tabs/tabs {:size           32
                      :on-change      #(reset! selected-tab %)
                      :default-active selected-tab
                      :data           [{:id    :all
                                        :label (i18n/label :t/all)}
                                       {:id    :open
                                        :label (i18n/label :t/open)}
                                       {:id    :gated
                                        :label (i18n/label :t/gated)}]}]]
    [rn/view {:flex-direction :row}
     [quo2.button/button
      {:icon     true
       :type     :outline
       :size     32
       :style    {:margin-right 12}
       :on-press #(re-frame/dispatch [:bottom-sheet/show-sheet :sort-communities {}])}
      :main-icons2/flash]
     [quo2.button/button
      {:icon     true
       :type     :outline
       :size     32
       :on-press #(if (= @view-type :card-view)
                    (reset! view-type :list-view)
                    (reset! view-type :card-view))}
      (if (= @view-type :card-view)
        :main-icons2/card-view
        :main-icons2/list-view)]]]])

(defn featured-communities [communities]
  [list/flat-list
   {:key-fn                          :id
    :horizontal                        true
    :getItemLayout                     get-item-layout-js
    :keyboard-should-persist-taps      :always
    :shows-horizontal-scroll-indicator false
    :data                              communities
    :render-fn                         render-featured-fn}])

(defn featured-communities-section [communities]
  (let [count (reagent/atom {:value (count communities) :type :grey})]
    [rn/view {:flex         1}
     [rn/view {:flex-direction  :row
               :height          30
               :padding-top     8
               :justify-content :space-between
               :padding-horizontal 20}
      [rn/view {:flex-direction  :row
                :align-items     :center}
       [quo2.text/text {:accessibility-label :featured-communities-title
                        :weight              :semi-bold
                        :size                :paragraph-1
                        :style               {:margin-right   6}}
        (i18n/label :t/featured)]
       [quo2.counter/counter @count (:value @count)]]
      [icons/icon :main-icons2/info {:container-style {:align-items     :center
                                                       :justify-content :center}
                                     :resize-mode      :center
                                     :size             20
                                     :color            (colors/theme-colors
                                                        colors/neutral-50
                                                        colors/neutral-40)}]]
     [rn/view {:margin-top     8}
      [featured-communities communities]]]))

(defn other-communitites-header [communities]
  [:<>
   [community-filter-tags]
   [featured-communities-section communities]
   (when communities
     [:<>
      [rn/view {:margin-vertical    4
                :padding-horizontal 20}
       [separator/separator]]
      [discover-community-segments]])])

(defn other-communities [communities sort-list-by]
  (let [sorted-communities (sort-by sort-list-by communities)]
    [list/flat-list
     {:key-fn                            :id
      :getItemLayout                     get-item-layout-js
      :keyboard-should-persist-taps      :always
      :shows-horizontal-scroll-indicator false
      :header                            (other-communitites-header communities)
      :data                              sorted-communities
      :render-fn                         render-other-fn}]))

(defn segments-community-lists [communities]
  (let [tab @selected-tab
        sort-list-by @sort-list-by]
    (case tab
      :all
      [other-communities communities sort-list-by]

      :open
      [other-communities communities sort-list-by]

      :gated
      [other-communities communities sort-list-by])))

(defn discover-communities []
  (let [communities (<sub [:communities/communities])]
    [rn/view {:flex             1}
     [quo2.button/button {:icon     true
                          :type     :grey
                          :size     32
                          :style    {:margin-vertical 12
                                     :margin-left     20}
                          :on-press #(>evt [:navigate-back])}
      :close]
     [title-and-search]
     [segments-community-lists communities]]))

(defn communities []
  (fn []
    [safe-area/consumer
     (fn []
       [rn/view {:style {:flex             1
                         :background-color (colors/theme-colors
                                            colors/white
                                            colors/neutral-90)}}
        [discover-communities]])]))
