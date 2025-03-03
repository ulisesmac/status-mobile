(ns status-im.chat.models.input
  (:require ["emojilib" :as emojis]
            [clojure.string :as string]
            [goog.object :as object]
            [re-frame.core :as re-frame]
            [status-im.chat.models :as chat]
            [status-im.chat.models.mentions :as mentions]
            [status-im.chat.models.message :as chat.message]
            [status-im.chat.models.message-content :as message-content]
            [status-im2.constants :as constants]
            [utils.re-frame :as rf]
            [utils.i18n :as i18n]
            [utils.datetime :as datetime]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log]
            [status-im.ui.screens.chat.components.input :as input]))

(defn text->emoji
  "Replaces emojis in a specified `text`"
  [text]
  (utils/safe-replace text
                      #":([a-z_\-+0-9]*):"
                      (fn [[original emoji-id]]
                        (if-let [emoji-map (object/get (.-lib emojis) emoji-id)]
                          (.-char ^js emoji-map)
                          original))))

(rf/defn set-chat-input-text
  "Set input text for current-chat. Takes db and input text and cofx
  as arguments and returns new fx. Always clear all validation messages."
  {:events [:chat.ui/set-chat-input-text]}
  [{db :db} new-input chat-id]
  (let [current-chat-id (or chat-id (:current-chat-id db))]
    {:db (assoc-in db [:chat/inputs current-chat-id :input-text] (text->emoji new-input))}))

(rf/defn set-timeline-input-text
  {:events [:chat.ui/set-timeline-input-text]}
  [{db :db} new-input]
  {:db (assoc-in db
        [:chat/inputs (chat/my-profile-chat-topic db) :input-text]
        (text->emoji new-input))})

(rf/defn select-mention
  {:events [:chat.ui/select-mention]}
  [{:keys [db] :as cofx} text-input-ref {:keys [alias name searched-text match] :as user}]
  (let [chat-id     (:current-chat-id db)
        new-text    (mentions/new-input-text-with-mention cofx user)
        at-sign-idx (get-in db [:chats/mentions chat-id :mentions :at-sign-idx])
        cursor      (+ at-sign-idx (count name) 2)]
    (rf/merge
     cofx
     {:db                   (-> db
                                (assoc-in [:chats/cursor chat-id] cursor)
                                (assoc-in [:chats/mention-suggestions chat-id] nil))
      :set-text-input-value [chat-id new-text text-input-ref]}
     (set-chat-input-text new-text chat-id)
     ;; NOTE(rasom): Some keyboards do not react on selection property passed to
     ;; text input (specifically Samsung keyboard with predictive text set on).
     ;; In this case, if the user continues typing after the programmatic change,
     ;; the new text is added to the last known cursor position before
     ;; programmatic change. By calling `reset-text-input-cursor` we force the
     ;; keyboard's cursor position to be changed before the next input.
     (mentions/reset-text-input-cursor text-input-ref cursor)
     ;; NOTE(roman): on-text-input event is not dispatched when we change input
     ;; programmatically, so we have to call `on-text-input` manually
     (mentions/on-text-input
      (let [match-len         (count match)
            searched-text-len (count searched-text)
            start             (inc at-sign-idx)
            end               (+ start match-len)]
        {:new-text      match
         :previous-text searched-text
         :start         start
         :end           end}))
     (mentions/recheck-at-idxs {alias user}))))

(defn- start-cooldown
  [{:keys [db]} cooldowns]
  {:dispatch-later        [{:dispatch [:chat/disable-cooldown]
                            :ms       (constants/cooldown-periods-ms
                                       cooldowns
                                       constants/default-cooldown-period-ms)}]
   :show-cooldown-warning nil
   :db                    (assoc db
                                 :chat/cooldowns               (if
                                                                 (=
                                                                  constants/cooldown-reset-threshold
                                                                  cooldowns)
                                                                 0
                                                                 cooldowns)
                                 :chat/spam-messages-frequency 0
                                 :chat/cooldown-enabled?       true)})

(rf/defn process-cooldown
  "Process cooldown to protect against message spammers"
  [{{:keys [chat/last-outgoing-message-sent-at
            chat/cooldowns
            chat/spam-messages-frequency
            current-chat-id]
     :as   db}
    :db
    :as cofx}]
  (when (chat/public-chat? cofx current-chat-id)
    (let [spamming-fast?       (< (- (datetime/timestamp) last-outgoing-message-sent-at)
                                  (+ constants/spam-interval-ms (* 1000 cooldowns)))
          spamming-frequently? (= constants/spam-message-frequency-threshold
                                  spam-messages-frequency)]
      (cond-> {:db (assoc db
                          :chat/last-outgoing-message-sent-at (datetime/timestamp)
                          :chat/spam-messages-frequency       (if spamming-fast?
                                                                (inc spam-messages-frequency)
                                                                0))}

        (and spamming-fast? spamming-frequently?)
        (start-cooldown (inc cooldowns))))))

(rf/defn reply-to-message
  "Sets reference to previous chat message and focuses on input"
  {:events [:chat.ui/reply-to-message]}
  [{:keys [db]} message]
  (let [current-chat-id (:current-chat-id db)]
    {:db (-> db
             (assoc-in [:chat/inputs current-chat-id :metadata :responding-to-message]
                       message)
             (assoc-in [:chat/inputs current-chat-id :metadata :editing-message] nil)
             (update-in [:chat/inputs current-chat-id :metadata]
                        dissoc
                        :sending-image))}))

(rf/defn edit-message
  "Sets reference to previous chat message and focuses on input"
  {:events [:chat.ui/edit-message]}
  [{:keys [db] :as cofx} message]
  (let [current-chat-id (:current-chat-id db)
        text            (get-in message [:content :text])]
    (rf/merge cofx
              {:db (-> db
                       (assoc-in [:chat/inputs current-chat-id :metadata :editing-message]
                                 message)
                       (assoc-in [:chat/inputs current-chat-id :metadata :responding-to-message] nil)
                       (update-in [:chat/inputs current-chat-id :metadata]
                                  dissoc
                                  :sending-image))}
              (input/set-input-text text current-chat-id))))

(rf/defn show-contact-request-input
  "Sets reference to previous chat message and focuses on input"
  {:events [:chat.ui/send-contact-request]}
  [{:keys [db]}]
  (let [current-chat-id (:current-chat-id db)]
    {:db (-> db
             (assoc-in [:chat/inputs current-chat-id :metadata :sending-contact-request]
                       current-chat-id)
             (assoc-in [:chat/inputs current-chat-id :metadata :responding-to-message]
                       nil)
             (assoc-in [:chat/inputs current-chat-id :metadata :editing-message] nil)
             (update-in [:chat/inputs current-chat-id :metadata]
                        dissoc
                        :sending-image))}))

(rf/defn cancel-message-reply
  "Cancels stage message reply"
  {:events [:chat.ui/cancel-message-reply]}
  [{:keys [db]}]
  (let [current-chat-id (:current-chat-id db)]
    {:db (assoc-in db [:chat/inputs current-chat-id :metadata :responding-to-message] nil)}))

(defn build-text-message
  [{:keys [db]} input-text current-chat-id]
  (when-not (string/blank? input-text)
    (let [{:keys [message-id]}
          (get-in db [:chat/inputs current-chat-id :metadata :responding-to-message])
          preferred-name       (get-in db [:multiaccount :preferred-name])
          emoji?               (message-content/emoji-only-content? {:text        input-text
                                                                     :response-to message-id})]
      {:chat-id      current-chat-id
       :content-type (if emoji?
                       constants/content-type-emoji
                       constants/content-type-text)
       :text         input-text
       :response-to  message-id
       :ens-name     preferred-name})))

(defn build-image-messages
  [{db :db} chat-id]
  (let [images   (get-in db [:chat/inputs chat-id :metadata :sending-image])
        album-id (str (random-uuid))]
    (mapv (fn [[_ {:keys [uri]}]]
            {:chat-id      chat-id
             :album-id     album-id
             :content-type constants/content-type-image
             :image-path   (utils/safe-replace uri #"file://" "")
             :text         (i18n/label :t/update-to-see-image {"locale" "en"})})
          images)))

(rf/defn clean-input
  [{:keys [db] :as cofx} current-chat-id]
  (rf/merge cofx
            {:db (-> db
                     (assoc-in [:chat/inputs current-chat-id :metadata :sending-contact-request] nil)
                     (assoc-in [:chat/inputs current-chat-id :metadata :sending-image] nil)
                     (assoc-in [:chat/inputs current-chat-id :metadata :editing-message] nil)
                     (assoc-in [:chat/inputs current-chat-id :metadata :responding-to-message] nil))}
            (set-chat-input-text nil current-chat-id)))

(rf/defn cancel-message-edit
  "Cancels stage message edit"
  {:events [:chat.ui/cancel-message-edit]}
  [{:keys [db] :as cofx}]
  (let [current-chat-id (:current-chat-id db)]
    (rf/merge cofx
              {:set-text-input-value [current-chat-id ""]}
              (clean-input current-chat-id)
              (mentions/clear-mentions)
              (mentions/clear-cursor))))

(rf/defn send-messages
  [{:keys [db] :as cofx} input-text current-chat-id]
  (let [image-messages (build-image-messages cofx current-chat-id)
        text-message   (build-text-message cofx input-text current-chat-id)
        messages       (keep identity (conj image-messages text-message))]
    (when (seq messages)
      (rf/merge cofx
                (clean-input (:current-chat-id db))
                (process-cooldown)
                (chat.message/send-messages messages)))))

(rf/defn send-my-status-message
  "when not empty, proceed by sending text message with public key topic"
  {:events [:profile.ui/send-my-status-message]}
  [{db :db :as cofx}]
  (let [current-chat-id      (chat/my-profile-chat-topic db)
        {:keys [input-text]} (get-in db [:chat/inputs current-chat-id])
        image-messages       (build-image-messages cofx current-chat-id)
        text-message         (build-text-message cofx input-text current-chat-id)
        messages             (keep identity (conj image-messages text-message))]
    (when (seq messages)
      (rf/merge cofx
                (clean-input current-chat-id)
                (chat.message/send-messages messages)))))

(rf/defn send-audio-message
  [cofx audio-path duration current-chat-id]
  (when-not (string/blank? audio-path)
    (chat.message/send-message
     cofx
     {:chat-id           current-chat-id
      :content-type      constants/content-type-audio
      :audio-path        audio-path
      :audio-duration-ms duration
      :text              (i18n/label :t/update-to-listen-audio {"locale" "en"})})))

(rf/defn send-sticker-message
  [cofx {:keys [hash packID pack]} current-chat-id]
  (when-not (or (string/blank? hash) (and (string/blank? packID) (string/blank? pack)))
    (chat.message/send-message cofx
                               {:chat-id      current-chat-id
                                :content-type constants/content-type-sticker
                                :sticker      {:hash hash
                                               :pack (int (if (string/blank? packID) pack packID))}
                                :text         (i18n/label :t/update-to-see-sticker {"locale" "en"})})))

(rf/defn send-edited-message
  [{:keys [db] :as cofx} text {:keys [message-id quoted-message]}]
  (rf/merge
   cofx
   {:json-rpc/call [{:method      "wakuext_editMessage"
                     :params      [{:id           message-id
                                    :text         text
                                    :content-type (if (message-content/emoji-only-content?
                                                       {:text text :response-to quoted-message})
                                                    constants/content-type-emoji
                                                    constants/content-type-text)}]
                     :js-response true
                     :on-error    #(log/error "failed to edit message " %)
                     :on-success  #(re-frame/dispatch [:sanitize-messages-and-process-response %])}]}
   (cancel-message-edit)
   (process-cooldown)))

(rf/defn send-current-message
  "Sends message from current chat input"
  {:events [:chat.ui/send-current-message]}
  [{{:keys [current-chat-id] :as db} :db :as cofx}]
  (let [{:keys [input-text metadata]} (get-in db [:chat/inputs current-chat-id])
        editing-message               (:editing-message metadata)
        input-text-with-mentions      (mentions/check-mentions cofx input-text)]
    (rf/merge cofx
              (if editing-message
                (send-edited-message input-text-with-mentions editing-message)
                (send-messages input-text-with-mentions current-chat-id))
              (mentions/clear-mentions)
              (mentions/clear-cursor))))

(rf/defn send-contact-request
  {:events [:contacts/send-contact-request]}
  [{:keys [db] :as cofx} public-key message]
  (rf/merge cofx
            {:chat.ui/clear-inputs     nil
             :chat.ui/clear-inputs-old nil
             :json-rpc/call            [{:method      "wakuext_sendContactRequest"
                                         :js-response true
                                         :params      [{:id public-key :message message}]
                                         :on-error    #(log/warn "failed to send a contact request" %)
                                         :on-success  #(re-frame/dispatch [:transport/message-sent %])}]}
            (mentions/clear-mentions)
            (mentions/clear-cursor)
            (clean-input (:current-chat-id db))
            (process-cooldown)))

(rf/defn cancel-contact-request
  "Cancels contact request"
  {:events [:chat.ui/cancel-contact-request]}
  [{:keys [db] :as cofx}]
  (let [current-chat-id (:current-chat-id db)]
    (rf/merge cofx
              {:db (assoc-in db [:chat/inputs current-chat-id :metadata :sending-contact-request] nil)}
              (mentions/clear-mentions)
              (mentions/clear-cursor)
              (clean-input (:current-chat-id db))
              (process-cooldown))))

(rf/defn chat-send-sticker
  {:events [:chat/send-sticker]}
  [{{:keys [current-chat-id] :as db} :db :as cofx} {:keys [hash packID pack] :as sticker}]
  (rf/merge
   cofx
   {:db            (update db
                           :stickers/recent-stickers
                           (fn [recent]
                             (conj (remove #(= hash (:hash %)) recent) sticker)))
    :json-rpc/call [{:method     "stickers_addRecent"
                     :params     [(int (if (string/blank? packID) pack packID)) hash]
                     :on-success #()}]}
   (send-sticker-message sticker current-chat-id)))

(rf/defn chat-send-audio
  {:events [:chat/send-audio]}
  [{{:keys [current-chat-id]} :db :as cofx} audio-path duration]
  (send-audio-message cofx audio-path duration current-chat-id))
