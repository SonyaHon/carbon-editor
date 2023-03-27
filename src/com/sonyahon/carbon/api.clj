(ns com.sonyahon.carbon.api
  (:require [com.sonyahon.carbon.font :as fonts]
            [com.sonyahon.carbon.editor :as editor]))

(defn set-font [family size]
  (let [font        (fonts/load-font family size)
        line-height (fonts/measure-text-height font)]
    (swap! editor/editor-state assoc
           :font font
           :line-height line-height)))
