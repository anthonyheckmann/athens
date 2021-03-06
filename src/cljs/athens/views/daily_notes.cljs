(ns athens.views.daily-notes
  (:require
    [athens.db :as db]
    [athens.style :refer [DEPTH-SHADOWS]]
    [athens.util :refer [get-day]]
    [athens.views.node-page :refer [node-page-component]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [goog.dom :refer [getElement]]
    [goog.functions :refer [debounce]]
    [posh.reagent :refer [pull-many]]
    [re-frame.core :refer [dispatch subscribe]]
    [stylefy.core :refer [use-style]]))


;;; Styles


(def daily-notes-scroll-area-style
  {:min-height "calc(100vh + 1px)"
   :display        "flex"
   :padding        "1.25rem 0"
   :align-items    "stretch"
   :flex           "1 1 100%"
   :flex-direction "column"})


(def daily-notes-page-style
  {:box-shadow (:16 DEPTH-SHADOWS)
   :align-self "stretch"
   :justify-self "stretch"
   :margin "1.25rem 2.5rem"
   :padding "1rem 2rem"
   :transition-duration "0s"
   :border-radius "0.5rem"
   :min-height "calc(100vh - 10rem)"})


(def daily-notes-notional-page-style
  (merge daily-notes-page-style {:box-shadow (:4 DEPTH-SHADOWS)
                                 :opacity "0.5"}))


;;; Helpers



(defn scroll-daily-notes
  [_]
  (let
    [daily-notes @(subscribe [:daily-notes/items])
     from-bottom (.. (getElement "daily-notes") getBoundingClientRect -bottom)
     doc-height (.. js/document -documentElement -scrollHeight)
     delta (- from-bottom doc-height)]
    (when (< delta 1)
      (dispatch [:daily-note/next (get-day (count daily-notes))]))))


(def db-scroll-daily-notes (debounce scroll-daily-notes 500))


;;; Components


(defn daily-notes-panel
  []
  (let [note-refs (subscribe [:daily-notes/items])]
    (fn []
      (if (empty? @note-refs)
        (dispatch [:daily-note/next (get-day)])
        (let [notes (some->> @note-refs
                             not-empty
                             (map (fn [x] [:block/uid x]))
                             (pull-many db/dsdb '[*])
                             deref)]
          [:div#daily-notes (use-style daily-notes-scroll-area-style)
           (doall
             (for [{:keys [block/uid]} notes]
               ^{:key uid}
               [:<>
                [:div (use-style daily-notes-page-style)
                 [node-page-component [:block/uid uid]]]]))
           [:div (use-style daily-notes-notional-page-style)
            [:h1 "Earlier"]]])))))
