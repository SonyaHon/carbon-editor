(ns com.sonyahon.carbon.utils
  (:import  [org.jetbrains.skija RRect Paint TextLine Font]))

(defn color [^long l]
  (.intValue (Long/valueOf l)))

(defn paint [c]
  (doto (Paint.)
    (.setAntiAlias true)
    (.setColor (color c))))

(defn text-line [text]
  (TextLine/make text (Font.)))

(defn round-rect
  ([x y width height radius]
   (round-rect x y width height radius radius radius radius))
  ([x y width height radius-top-left radius-top-right radius-bottom-left radius-bottom-right]
   (RRect. x y width height (float-array [radius-top-left
                                          radius-top-right
                                          radius-bottom-left
                                          radius-bottom-right]))))
