(ns quo2.components.community.discover-card
  (:require
   [status-im.react-native.resources :as resources]
   [quo2.components.markdown.text :as text]
   [quo2.foundations.colors :as colors]
   [status-im.ui.screens.communities.styles :as styles]
   [status-im.ui.components.react :as react]))

(def images
  [{:id 1   :image   (resources/get-image :placeholder-image1)}
   {:id 2   :image   (resources/get-image :placeholder-image2)}
   {:id 3   :image   (resources/get-image :placeholder-image3)}
   {:id 4   :image   (resources/get-image :placeholder-image7)}
   {:id 5   :image   (resources/get-image :placeholder-image8)}])

(defn compare-image-id [id image]
  (= id (:id image)))

(defn card-title-and-description [title description joined?]
  [react/view
   {:flex               1
    :justify-content    :center
    :margin-top         (if joined? 8 10)
    :margin-bottom      (if joined? 8 2)
    :border-radius      12}
   [react/view {:flex           1
                :padding-horizontal 12}
    [text/text {:accessibility-label :community-name-text
                :ellipsize-mode      :tail
                :number-of-lines     1
                :weight              :semi-bold
                :size                :heading-2
                :margin-bottom       2}
     title]
    [text/text {:accessibility-label :community-name-text
                :ellipsize-mode      :tail
                :number-of-lines     1
                :color               (colors/theme-colors
                                      colors/neutral-50
                                      colors/neutral-40)
                :weight               :regular
                :size                 :paragraph-1}
     description]]])

(defn placeholder-images [{:keys [images size border-radius joined?]
                           :or {joined? false}}]
  [react/view
   [react/view (if joined?
                 {:justify-content    :center}
                 {:flex-direction     :row})
    (for [{:keys [id image]} images]
      ^{:key id}
      [react/image {:source image
                    :style  {:border-radius border-radius
                             :margin-top    (if joined? 4 8)
                             :margin-right  (if joined?
                                              4 8)
                             :width         size
                             :height        size
                             :resize-mode   :contain}}])]])

(defn discover-card [{:keys [title description joined?] :or {joined? false}}]
  (let [first-row-images (take (if joined? 3 3) images)
        second-row-images (take (if joined? 2 2) (subvec images 3))
        size             (if joined? 32 80)
        border-radius    (if joined?  6 20)]
    [react/view {:flex              1
                 :margin-horizontal 20}
     [react/view (merge (styles/community-card 16)
                        {:background-color (colors/theme-colors
                                            colors/white
                                            colors/neutral-90)}
                        (if joined?
                          {:flex-direction :row
                           :height         56}
                          {:padding-bottom 12}))
      [card-title-and-description title description joined?]
      [react/view (if joined?
                    {:margin-top  -20}
                    {:flex-direction :row})
       [react/image {:source (resources/get-image :placeholder-image5)
                     :style  {:border-top-right-radius border-radius
                              :border-bottom-right-radius border-radius
                              :margin-top    (if joined? 4 8)
                              :margin-right  (if joined? 4 8)
                              :width         32.5
                              :height        80
                              :resize-mode   :contain}}]
       [placeholder-images {:images        first-row-images
                            :size          size
                            :border-radius border-radius
                            :joined?       joined?}]
       [react/image {:source (resources/get-image :placeholder-image4)
                     :style  {:border-top-left-radius border-radius
                              :border-bottom-left-radius border-radius
                              :margin-top    (if joined? 4 8)
                              :width         32.5
                              :height        80
                              :resize-mode   :contain}}]]
      [react/view (when-not joined?
                    {:flex-direction :row})
       [react/image {:source (resources/get-image :placeholder-image6)
                     :style  {:border-top-right-radius border-radius
                              :border-bottom-right-radius border-radius
                              :margin-top    (if joined? 4 8)
                              :margin-right  (if joined? 4 8)
                              :width         75.5
                              :height        size
                              :resize-mode   :contain}}]
       [placeholder-images {:images        second-row-images
                            :size          size
                            :border-radius border-radius
                            :joined?       joined?}]
       [react/image {:source (resources/get-image :placeholder-image9)
                     :style  {:border-top-left-radius border-radius
                              :border-bottom-left-radius border-radius
                              :margin-top    (if joined? 4 8)
                              :width         75.5
                              :height        size
                              :resize-mode   :contain}}]]]]))

