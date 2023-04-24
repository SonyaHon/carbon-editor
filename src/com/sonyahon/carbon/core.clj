(ns com.sonyahon.carbon.core
  (:require [com.sonyahon.carbon.cli :as cli]
            [com.sonyahon.carbon.gfx.core :as gfx]
            [com.sonyahon.carbon.utils.clojure :as clj]
            [com.sonyahon.carbon.editor.core :as editor]
            [com.sonyahon.carbon.utils.repl :as repl]
            [com.sonyahon.carbon.defaults.core :as defaults]))

(set! *warn-on-reflection* true)

(defn- render [canvas width height]
  (editor/render canvas width height))

(defn- key-callback [window key scancode action mods]
  (editor/handle-key-event window key scancode action mods))

(defn- char-callback [window codepoint]
  (editor/handle-char-event window codepoint))

(defn -main [& args]
  (let [parsed (cli/parse args)]
    (if (get-in parsed [:options :help])
      (do
        (println "Usage:\n  carbon [Options] [Path]\n")
        (println "Options:")
        (println (get parsed :summary)))
      (do
        (gfx/set-glfw-error-callback)
        (gfx/glfw-init)
        (gfx/set-glfw-window-hints)

        (let [width  (get-in parsed [:options :window-width])
              height (get-in parsed [:options :window-height])
              window (gfx/glfw-create-window width height)]
          (gfx/glfw-setup-capabilities window)
          (clj/run-clojure-in-thread)
          (repl/start-nrepl)
          (let [context           (gfx/create-gl-context)
                fb-id             (gfx/get-frame-buffer-id)
                [scale-x scale-y] (gfx/get-display-scale window)
                render-target     (gfx/get-render-target scale-x scale-y width height fb-id)
                render-surface    (gfx/get-render-surface context render-target)
                canvas            (gfx/get-canvas render-surface)]
            (gfx/scale-canvas canvas scale-x scale-y)
            (gfx/set-key-callback window #(#'key-callback %1 %2 %3 %4 %5))
            (gfx/set-char-callback window #(#'char-callback %1 %2))

            (defaults/setup)

            (loop []
              (when (gfx/glfw-app-running? window)
                (let [layer (gfx/save-canvas canvas)]
                  (#'render canvas width height)
                  (gfx/restore-canvas canvas layer))
                (gfx/flush-context context)
                (gfx/glfw-end-loop window)
                (recur)))
            (gfx/terminate window)))))))

