(ns storm-blog.comps
  (:require [rum.core :as rum]
            [om.core :as om :include-macros true]
            [datascript.core :as d]
            [garden.core :refer [css]]
            [storm-blog.db :as db]
            [storm-blog.css :as style]
            [storm-blog.util :as u]))

;; prevent cursor-ification
;; pulled from https://gist.github.com/swannodette/11308901
#_ (extend-type d/DB
     om/IToCursor
     (-to-cursor
      ([this _] this)
      ([this _ _] this)))

(defn get-conn [owner]
  (om/get-shared owner :conn))

(defn val-sel [selector ] (u/value (u/select selector)))


(defn comment-section [db {own :comment/owner ema :comment/email
                               com :comment/content web :comment/website eid :db/id}]
    [:.comment
     [:.owner own] [:.ema ema]
     [:.web web] [:.com com]])


(defn add-comment [conn article-eid]
  (d/transact! conn [{:db/id article-eid
                 :article/comment {:comment/owner "Bob"
                                   :comment/email "email"
                                   :comment/website "website"
                                   :comment/content "Content"}}]))

(defn extract-comment [article-eid]
  {:db/id article-eid
   :article/comment {:comment/owner    (val-sel ".add-owner")
                     :comment/email    (val-sel ".add-email")
                     :comment/website  (val-sel ".add-website")
                     :comment/content  (val-sel ".add-comment")}})

(defn comment-form [db {article-eid :db/id :or {article-eid 1}}]
  [:form {:on-submit (fn [_] (js/alert (extract-comment article-eid)) false)}
   [:input.add-owner   {:type :text :placeholder :Owner}]
   [:input.add-email   {:type :text :placeholder :Email}]
   [:input.add-website {:type :text :placeholder :Website}]
   [:input.add-comment {:type :text :placeholder :Comment}]
   [:input.add-submit  {:type "submit" :value "Add comment"}]])

(defn demo-card [db {title :card/title words :card/words buttons :card/buttons eid :db/id
                :or {title "No Title" words "Words Error" buttons ["error0" "error1" "error2" "erorr3" "error4"] eid 2}
                :as state}]
    [:.demo-card-square.mdl-card.mdl-shadow--2dp
     [:.mdl-card__title.mdl-card--expand
      [:h2.mdl-card__title-text title]]
     [:.mdl-card__supporting-text words]
     [:.mdl-card__actions.mdl-card--border
      (map #(vector :a.mdl-button.mdl-button--colored.mdl-js-button.mdl-js-ripple-effect %) buttons)]])

(defn article [db {title :article/title a-list :article/list
                       content :article/content category :article/category eid :db/id
                   :or {title "Error: No title" a-list ["Error: No description list for article"]
                        content ["Error: No content for article"] category "Error: No category for article"
                        eid 1}
                   :as state}]
    [:.mdl-grid
     [:.article-spacer.mdl-cell.mdl-cell--2-col.mdl-cell--hide-table.mdl-cell--hide-phone]
     [:article.demo-content.mdl-color--white.mdl-shadow--4dp.content.mdl-color-text--grey-800.mdl-cell.mdl-cell--8-col
      [:.article-header
       [:.article-banner] [:.article-category category] [:.article-title title]
       [:ul.article-list (map #(vector :li.article-list-item %) a-list)]]
      [:section (map #(vector :p %) content)]
      [:.mdl-layout-spacer]
       [:comment-section (map #(comment-section db (first %)) (db/pvea db eid :article/comment))
        (comment-form db state)]]])

(defn grid [db]
  [:.mdl-grid.demo-main
   #_ [:.article-spacer.mdl-cell.mdl-cell--2-col.mdl-cell--hide-table.mdl-cell--hide-phone]
   (map #(article db (first %)) (db/pea db :article/title))
   (map #(vector :.mdl-cell.mdl-cell--4-col
                 (demo-card db (first %))) (db/pea db :card/title))])

(defn content [db]
  [:main.mdl-layout__content [:.page-content (grid db)]])

(defn nav [db {links :nav/links}]
  [:nav.mdl-navigation (map #(vector :a.mdl-navigation__link {:href ""} %) links)])

(defn header [db]
  [:.header.mdl-layout__header [:.mdl-layout__header-row (nav db (ffirst (db/pea db :nav/links)))]])

(defn drawer [db]
  [:.mdl-layout__drawer [:span.mdl-layout-title "Title" ] (nav db (ffirst (db/pea db :nav/links)))])

(defn page [db]
  [:.mdl-layout.mdl-js-layout.mdl-layout--fixed-header.canvas
   style/page-css
   (header db)
   (drawer db)
   (content db)])

(defn app-view [db owner]
  (reify
    om/IRender
    (render [_]
      (page db))))




