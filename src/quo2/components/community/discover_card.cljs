(ns quo2.components.community.discover-card
  (:require
   [status-im.react-native.resources :as resources]
   [quo2.components.markdown.text :as text]
   [quo2.foundations.colors :as colors]
   [status-im.ui.screens.communities.styles :as styles]
   [status-im.ui.components.react :as react]))

(def images
  {:joined     [{:id 1 :row-images    [{:id 1   :image   (resources/get-image :placeholder-image2)}
                                       {:id 2   :image   (resources/get-image :placeholder-image3)}
                                       {:id 3   :image   (resources/get-image :placeholder-image4)}]}
                {:id 2 :row-images    [{:id 4   :image   (resources/get-image :placeholder-image7)}
                                       {:id 5   :image   (resources/get-image :placeholder-image8)}]}]
   :not-joined [{:id 1 :column-images [{:id 1   :image   (resources/get-image :placeholder-image11)}]}
                {:id 2 :column-images [{}]}]})

(defn card-title-and-description [title description joined]
  [react/view
   {:flex               1
    :padding-top        (if joined 8 10)
    :padding-bottom     (if joined 8 2)
    :border-radius      12}
   [react/view {:flex                1
                :padding-horizontal  12}
    [text/text {:accessibility-label :community-name-text
                :ellipsize-mode      :tail
                :number-of-lines     1
                :weight              :semi-bold
                :size                (if joined
                                       :paragraph-1
                                       :heading-2)}
     title]
    [text/text {:accessibility-label :community-name-text
                :ellipsize-mode      :tail
                :number-of-lines     1
                :color               (colors/theme-colors
                                      colors/neutral-50
                                      colors/neutral-40)
                :weight               :regular
                :size                 (if joined
                                        :paragraph-2
                                        :paragraph-1)}
     description]]])

(defn placeholder-list-images [{:keys [images width height border-radius joined]
                                :or {joined false}}]
  [react/view
   [react/view (if joined
                 {:justify-content    :center}
                 {:flex-direction     :row})
    (for [{:keys [id image]} images]
      ^{:key id}
      [react/image {:source image
                    :style  {:border-radius border-radius
                             :margin-top    (if joined 4 8)
                             :margin-right  (if joined
                                              4 8)
                             :width         width
                             :height        height
                             :resize-mode   :contain}}])]])

(defn placeholder-row-images [{:keys [first-image last-image images width height
                                      border-radius joined]}]
  [react/view (when-not joined
                {:flex-direction :row})
   (when first-image
     [react/image {:source first-image
                   :style  {:border-top-right-radius    (when-not joined
                                                          border-radius)
                            :border-bottom-right-radius border-radius
                            :border-bottom-left-radius  (when joined
                                                          border-radius)
                            :margin-top                 (when-not joined 8)
                            :margin-right               (if joined 4 8)
                            :width                      width
                            :height                     height}}])
   (when images
     [placeholder-list-images {:images        images
                               :width         (if joined 32 80)
                               :height        (if joined 32 80)
                               :border-radius border-radius
                               :joined        joined}])
   (when last-image
     [react/image {:source last-image
                   :style  {:border-top-left-radius    border-radius
                            :border-bottom-left-radius (when-not joined
                                                         border-radius)
                            :border-top-right-radius (when joined
                                                       border-radius)
                            :margin-top                (if joined 4 8)
                            :width                     width
                            :height                    height}}])])

(defn discover-card [{:keys [title description joined] :or {joined false}}]
  (let [on-joined-images     (get images   :joined)
        on-not-joined-images (get images   :not-joined)
        border-radius        (if  joined   6  20)]
    [react/view {:flex              1
                 :margin-horizontal 20}
     [react/view (merge (styles/community-card 16)
                        {:background-color  (colors/theme-colors
                                             colors/white
                                             colors/neutral-90)}
                        (if joined
                          {:flex-direction  :row
                           :height          56
                           :padding-right   12}
                          {:padding-bottom  12}))
      [card-title-and-description title description joined]
      (if joined
        (for [{:keys [id column-images]} on-not-joined-images]
          ^{:key id}
          [placeholder-row-images {:images        (when (= id 1)
                                                    column-images)
                                   :width         32
                                   :height        (if (= id 1) 8 26)
                                   :border-radius border-radius
                                   :joined        joined
                                   :first-image   (if (= id 1)
                                                    (resources/get-image :placeholder-image12)
                                                    (resources/get-image :placeholder-image13))
                                   :last-image    (if (= id 1)
                                                    (resources/get-image :placeholder-image10)
                                                    (resources/get-image :placeholder-image14))}])
        (for [{:keys [id row-images]} on-joined-images]
          ^{:key id}
          [placeholder-row-images {:images        row-images
                                   :width         (if (= id 1) 32.5 75.5)
                                   :height        80
                                   :border-radius border-radius
                                   :joined        joined
                                   :first-image   (if (= id 1)
                                                    (resources/get-image :placeholder-image1)
                                                    (resources/get-image :placeholder-image6))
                                   :last-image    (if (= id 1)
                                                    (resources/get-image :placeholder-image5)
                                                    (resources/get-image :placeholder-image9))}]))]]))

