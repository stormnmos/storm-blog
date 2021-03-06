(ns storm-blog.actions
  (:require
   [storm-blog.db :as db]
   [cljs.core.async :as async :refer [<! >! put! take!]]
   [om.core :as om])
  
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn transact! [events & data]
  (go (>! events data)))

(defn add-text [eid events owner order tag]
  (go (>! events [{:db/id -1 :widget/type :text
                   :widget/content "New"
                   :widget/owner owner
                   :widget/order order
                   :widget/tag tag}])))

(defn ->par [db eid events]
  (go (>! events [{:db/id eid :widget/tag :p}])))

(defn ->section [db eid events]
  (go (>! events [{:db/id eid :widget/tag :h3}])))

(defn retract [_ eid events]
  (go (>! events [[:db.fn/retractEntity eid]])))

(defn not-active [owner]
  {:on-click       #(om/set-state! owner :show-dropdown true)
   :on-mouse-leave #(om/set-state! owner :show-dropdown false)})

(defn active [owner]
  {:on-mouse-enter #(om/set-state! owner :show-dropdown true)
   :on-mouse-leave #(om/set-state! owner :show-dropdown false)})
