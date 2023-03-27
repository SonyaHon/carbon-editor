(ns com.sonyahon.carbon.editor
  (:require [clojure.stacktrace :as stacktrace]
            [com.sonyahon.carbon.utils :as utils]
            [com.sonyahon.carbon.buffer :as buffer]
            [com.sonyahon.carbon.font :as fonts])
  (:import
   [org.lwjgl.glfw GLFW]))
;;
;;
;; State
;;
;;

(let [default-font (fonts/load-font "JetBrains Mono" 16)]
  (def editor-state (atom {:buffers        [(buffer/empty-buffer)]
                           :current-buffer 0

                           :font           default-font
                           :line-height    (+ 4 (fonts/measure-text-height default-font))
                           :font-metrics   (fonts/get-font-metrics default-font)

                           :border         [8 8 8 8]
                           :canvas-padding [16 16 16 16]

                           :bg-color       (utils/paint 0xfff32ff3)
                           :canvas-color   (utils/paint 0xff181818)

                           :theme          {:foreground (utils/paint 0xffffffff)}})))
;;
;;
;; State getters
;;
;;

(defn get-current-font []
  (:font @editor-state))

(defn get-line-height []
  (:line-height @editor-state))

(defn get-bg-color []
  (:bg-color @editor-state))

(defn get-canvas-color []
  (:canvas-color @editor-state))

(defn get-current-buffer []
  (get-in @editor-state [:buffers (:current-buffer @editor-state)]))

(defn get-borders []
  (:border @editor-state))

(defn get-text-render-offsets []
  (map + (:border @editor-state) (:canvas-padding @editor-state)))

;;
;;
;; State setters
;;
;;

(defn set-current-buffer [new-buffer]
  (swap! editor-state assoc-in [:buffers (:current-buffer @editor-state)] new-buffer))

;;
;;
;; Renderers
;;
;;

(defn- render-bg
  "Render main bg"
  [canvas width height]
  (.drawRRect canvas
              (utils/round-rect 0 0 width height 12)
              (get-bg-color)))

(defn- render-canvas
  "Render actual buffer canvas"
  [canvas width height]
  (let [[x y w h] (get-borders)]
    (.drawRRect canvas
                (utils/round-rect x y (- width w) (- height h) 8)
                (get-canvas-color))))

(defn- render-current-buffer [canvas width height]
  (let [buff                (get-current-buffer)
        font                (get-current-font)
        {ascent :ascent
         descent :descent}  (:font-metrics @editor-state)
        line-height         (get-line-height)
        [x-offset y-offset] (get-text-render-offsets)
        y-offset            (+ y-offset (get-in @editor-state [:canvas-padding 1]))
        foreground          (get-in @editor-state [:theme :foreground])
        cursor-foreground   (get-bg-color)
        cursor              (:point buff)
        text-lines          (map-indexed vector (:lines buff))
        cursor-rendered     (atom false)]
    (doseq [[line-index
             {start :start
              end   :end}] text-lines]
      (let [line-text (buffer/get-range buff start end)
            text-y    (+ y-offset (* line-index line-height))]
        (.drawString canvas line-text x-offset text-y font foreground)
        (when (<= start cursor end)
          (reset! cursor-rendered true)
          (let [cursor-x (+ x-offset (fonts/measure-text-width font (subs line-text 0 (- cursor start))))
                cursor-y (+ text-y ascent -1)
                text-under-cursor (buffer/get-range buff cursor (inc cursor))
                cursor-width (fonts/measure-text-width font text-under-cursor)]
            (reset! cursor-rendered true)
            (.drawRRect canvas (utils/round-rect
                                cursor-x
                                (+ 0 cursor-y)
                                (+ cursor-width cursor-x)
                                (+ cursor-y (* -1 ascent) descent 2)
                                2) foreground)
            (.drawString canvas
                         text-under-cursor
                         cursor-x
                         text-y
                         font
                         cursor-foreground)))))
    (when (not @cursor-rendered)
      (let [cursor-x x-offset
            cursor-y (+ y-offset (* line-height (dec (count text-lines))))
            cursor-width 10]
        (.drawRRect canvas (utils/round-rect
                            cursor-x
                            cursor-y
                            (+ cursor-width cursor-x)
                            (+ cursor-y line-height)
                            2) foreground)))))

(defn- render-impl
  "Render implementation"
  [canvas width height]
  (render-bg canvas width height)
  (render-canvas canvas width height)
  (render-current-buffer canvas width height))

;;
;;
;; Public interface for rendering
;;
;;

(defn render [canvas width height]
  (try
    (render-impl canvas width height)
    (catch Exception e
      (stacktrace/print-stack-trace (stacktrace/root-cause e)))))

;;
;;
;; Event Handlers
;;
;;

(defn handle-key-event
  "Handle single key press handler (handles super, return, ecs, etc...)"
  [win key scancode action mods]
  (when (or (= action GLFW/GLFW_PRESS) (= action GLFW/GLFW_REPEAT))
    (condp = scancode
      ;; return
      36 (set-current-buffer
          (buffer/insert (get-current-buffer) "\n" (:point (get-current-buffer))))
      ;; meta
      55 (prn (get-current-buffer))
      ;; backspace
      51 (set-current-buffer
          (buffer/remove-backwards (get-current-buffer) 1 (:point (get-current-buffer))))
      ;; left arrow
      123 (set-current-buffer
           (buffer/move-point (get-current-buffer) -1 0))
      ;; right arrow
      124 (set-current-buffer
           (buffer/move-point (get-current-buffer) 1 0))
      ;; down arrow
      125 (set-current-buffer
           (buffer/move-point (get-current-buffer) 0 1))
      ;; up arrow
      126 (set-current-buffer
           (buffer/move-point (get-current-buffer) 0 -1))
      ;; default
      (prn scancode))))

(defn handle-char-event
  "Handle text input"
  [_win codepoint]
  (let [text (str (char codepoint))]
    (set-current-buffer
     (buffer/insert (get-current-buffer) text (:point (get-current-buffer))))))
