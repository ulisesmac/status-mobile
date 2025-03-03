(ns status-im.utils.universal-links.core
  (:require [clojure.string :as string]
            [goog.string :as gstring]
            [re-frame.core :as re-frame]
            [status-im.add-new.db :as new-chat.db]
            [status-im.chat.models :as chat]
            [status-im2.constants :as constants]
            [status-im.ethereum.core :as ethereum]
            [status-im.group-chats.core :as group-chats]
            [utils.i18n :as i18n]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.router.core :as router]
            [status-im.ui.components.react :as react]
            [utils.re-frame :as rf]
            [status-im.wallet.choose-recipient.core :as choose-recipient]
            [status-im2.navigation.events :as navigation]
            [taoensso.timbre :as log]
            [status-im.native-module.core :as status]))

;; TODO(yenda) investigate why `handle-universal-link` event is
;; dispatched 7 times for the same link

;; domains should be without the trailing slash
(def domains
  {:external "https://join.status.im"
   :internal "status-im:/"})

(def links
  {:public-chat        "%s/%s"
   :private-chat       "%s/p/%s"
   :community-requests "%s/cr/%s"
   :community          "%s/c/%s"
   :group-chat         "%s/g/%s"
   :user               "%s/u/%s"
   :browse             "%s/b/%s"})

(defn generate-link
  [link-type domain-type param]
  (gstring/format (get links link-type)
                  (get domains domain-type)
                  param))

(defn universal-link?
  [url]
  (boolean
   (re-matches constants/regx-universal-link url)))

(defn deep-link?
  [url]
  (boolean
   (re-matches constants/regx-deep-link url)))

(rf/defn handle-browse
  [cofx {:keys [url]}]
  (log/info "universal-links: handling browse" url)
  {:browser/show-browser-selection url})

(rf/defn handle-group-chat
  [cofx params]
  (log/info "universal-links: handling group" params)
  (group-chats/create-from-link cofx params))

(rf/defn handle-private-chat
  [{:keys [db] :as cofx} {:keys [chat-id]}]
  (log/info "universal-links: handling private chat" chat-id)
  (when chat-id
    (if-not (new-chat.db/own-public-key? db chat-id)
      (chat/start-chat cofx chat-id nil)
      {:utils/show-popup {:title   (i18n/label :t/unable-to-read-this-code)
                          :content (i18n/label :t/can-not-add-yourself)}})))

(rf/defn handle-community-requests
  [cofx {:keys [community-id]}]
  (log/info "universal-links: handling community request  " community-id)
  (navigation/navigate-to-cofx cofx :community-requests-to-join {:community-id community-id}))

(rf/defn handle-community
  [cofx {:keys [community-id]}]
  (log/info "universal-links: handling community" community-id)
  (navigation/navigate-to-cofx cofx :community {:community-id community-id}))


(rf/defn handle-navigation-to-desktop-community-from-mobile
  {:events [:handle-navigation-to-desktop-community-from-mobile]}
  [{:keys [db]} cofx deserialized-key]
  (navigation/navigate-to-cofx cofx :community {:community-id deserialized-key}))


(rf/defn handle-desktop-community
  [cofx {:keys [community-id]}]
  (status/deserialize-and-compress-key
   community-id
   (fn [deserialized-key]
     (rf/dispatch [:handle-navigation-to-desktop-community-from-mobile cofx (str deserialized-key)]))))

(rf/defn handle-community-chat
  [cofx {:keys [chat-id]}]
  (log/info "universal-links: handling community chat" chat-id)
  {:dispatch [:chat.ui/navigate-to-chat-nav2 chat-id]})

(rf/defn handle-public-chat
  [cofx {:keys [topic]}]
  (log/info "universal-links: handling public chat" topic)
  (when (seq topic)
    (chat/start-public-chat cofx topic)))

(rf/defn handle-view-profile
  [{:keys [db] :as cofx} {:keys [public-key ens-name]}]
  (log/info "universal-links: handling view profile" public-key)
  (cond
    (and public-key (new-chat.db/own-public-key? db public-key))
    {:change-tab-fx      :profile
     :pop-to-root-tab-fx :profile-stack}

    public-key
    (navigation/navigate-to-cofx (-> cofx
                                     (assoc-in [:db :contacts/identity] public-key)
                                     (assoc-in [:db :contacts/ens-name] ens-name))
                                 :profile
                                 {})))

(rf/defn handle-eip681
  [cofx data]
  (rf/merge cofx
            (choose-recipient/parse-eip681-uri-and-resolve-ens data true)
            (navigation/navigate-to-cofx :wallet nil)))

(defn existing-account?
  [{:keys [db]} address]
  (when address
    (some #(when (= (string/lower-case (:address %))
                    (string/lower-case address))
             %)
          (:multiaccount/accounts db))))

(rf/defn handle-wallet-account
  [cofx {address :account}]
  (when-let [account (existing-account? cofx address)]
    (navigation/navigate-to-cofx cofx
                                 :wallet-account
                                 account)))

(defn handle-not-found
  [full-url]
  (log/info "universal-links: no handler for " full-url))

(defn dispatch-url
  "Dispatch url so we can get access to re-frame/db"
  [url]
  (if-not (nil? url)
    (re-frame/dispatch [:universal-links/handle-url url])
    (log/debug "universal-links: no url")))

(rf/defn on-handle
  {:events [::match-value]}
  [cofx url {:keys [type] :as data}]
  (case type
    :group-chat         (handle-group-chat cofx data)
    :public-chat        (handle-public-chat cofx data)
    :private-chat       (handle-private-chat cofx data)
    :community-requests (handle-community-requests cofx data)
    :community          (handle-community cofx data)
    :desktop-community  (handle-desktop-community cofx data)
    :community-chat     (handle-community-chat cofx data)
    :contact            (handle-view-profile cofx data)
    :browser            (handle-browse cofx data)
    :eip681             (handle-eip681 cofx data)
    :wallet-account     (handle-wallet-account cofx data)
    (handle-not-found url)))

(rf/defn route-url
  "Match a url against a list of routes and handle accordingly"
  [{:keys [db]} url]
  {::router/handle-uri {:chain (ethereum/chain-keyword db)
                        :chats (:chats db)
                        :uri   url
                        :cb    #(re-frame/dispatch [::match-value url %])}})

(rf/defn store-url-for-later
  "Store the url in the db to be processed on login"
  [{:keys [db]} url]
  (log/info :store-url-for-later)
  {:db (assoc db :universal-links/url url)})

(rf/defn handle-url
  "Store url in the database if the user is not logged in, to be processed
  on login, otherwise just handle it"
  {:events [:universal-links/handle-url]}
  [{:keys [db] :as cofx} url]
  (if (and (multiaccounts.model/logged-in? cofx) (= (:app-state db) "active"))
    (route-url cofx url)
    (store-url-for-later cofx url)))

(rf/defn process-stored-event
  "Return an event description for processing a url if in the database"
  [{:keys [db] :as cofx}]
  (when-let [url (:universal-links/url db)]
    (rf/merge cofx
              {:db (dissoc db :universal-links/url)}
              (handle-url url))))

(defn unwrap-js-url
  [e]
  (-> e
      (js->clj :keywordize-keys true)
      :url))

(def url-event-listener
  (comp dispatch-url unwrap-js-url))

(defn initialize
  "Add an event listener for handling background->foreground transition
  and handles incoming url if the app has been started by clicking on a link"
  []
  (log/debug "universal-links: initializing")
  ;;NOTE: https://github.com/facebook/react-native/issues/15961
  ;; workaround for getInitialURL returning null when opening the
  ;; app from a universal link after closing it with the back button
  (js/setTimeout #(-> (.getInitialURL ^js react/linking)
                      (.then dispatch-url))
                 200)
  (.addEventListener ^js react/linking "url" url-event-listener))

(defn finalize
  "Remove event listener for url"
  []
  (log/debug "universal-links: finalizing")
  (.removeEventListener ^js react/linking "url" url-event-listener))
