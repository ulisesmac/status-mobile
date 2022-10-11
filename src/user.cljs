(ns user
  (:require [re-frame.core :as rf]
            [re-frame.db :as rf.db]
            [status-im.utils.datetime :as utils.datetime]))

(defn db []
  @rf.db/app-db)

(rf/reg-event-db
 :dev/reset-activity-center
 (fn [db]
   (-> db
       (assoc-in [:activity-center]
                 {:filter        {:status :unread
                                  :type   5}
                  :notifications {}}))))

(rf/reg-event-db
 :dev/assoc-in
 (fn [db [_ keys value]]
   (assoc-in db keys value)))

(comment
  (rf/dispatch [:dev/reset-activity-center]))
