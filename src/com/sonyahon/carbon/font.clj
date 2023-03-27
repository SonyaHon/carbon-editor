(ns com.sonyahon.carbon.font
  (:import [org.jetbrains.skija Font FontMgr Typeface FontStyle]))

(defn load-font
  "Create a new font."
  [family size]
  (let [mgr  (FontMgr/getDefault)
        face (.matchFamilyStyle mgr family FontStyle/NORMAL)]
    (Font. face (float size))))

(defn measure-text-width
  "Measure provided text width, including trailing spaces"
  ([font text]
   (let [wrapped-len (.getWidth (.measureText font "**"))]
     (measure-text-width font text "*" wrapped-len)))
  ([font text wrap-str wrap-str-len]
   (let [wrapped     (str wrap-str text wrap-str)
         wrapped-len (.getWidth (.measureText font wrapped))]
     (- wrapped-len wrap-str-len))))

(defn measure-text-height
  "Measure text height"
  [font]
  (-> font
      (.measureText "{(|A[")
      (.getHeight)))

(defn get-font-metrics [font]
  (let [metrics (.getMetrics font)]
    {:ascent         (.-_ascent metrics)
     :descent        (.-_descent metrics)
     :leading        (.-_leading metrics)
     :avg-char-width (.-_avgCharWidth metrics)
     :max-char-width (.-_maxCharWidth metrics)}))

(comment

  (load-font "Input Mono" 16)

  (measure-text-width (load-font "Input Mono" 16) " ")
  (measure-text-height (load-font "Input Mono" 16))

  (.-_maxCharWidth)

  (let [{ascent :ascent descent :descent} (get-font-metrics (load-font "Input Mono" 16))]
    (+ (abs ascent) (abs descent)))

  (.getSpacing (load-font "Input Mono" 16))

  (.getMetrics (load-font "Input Mono" 16))

  69)
