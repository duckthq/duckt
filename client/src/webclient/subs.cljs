(ns webclient.subs
  (:require
   [re-frame.core :as rf]))

(rf/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

(rf/reg-sub
 :is-mac?
 (fn [db _]
   (:is-mac? db)))
