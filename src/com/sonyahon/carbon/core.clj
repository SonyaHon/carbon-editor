(ns com.sonyahon.carbon.core
  (:require [com.sonyahon.carbon.repl :as repl]
            [com.sonyahon.carbon.editor :as editor]
            [com.sonyahon.carbon.cli :as cli])
  (:import [org.jetbrains.skija BackendRenderTarget Canvas ColorSpace DirectContext FramebufferFormat Surface SurfaceColorFormat SurfaceOrigin]
           [org.lwjgl.glfw Callbacks GLFW GLFWErrorCallback GLFWKeyCallback GLFWCharCallback]
           [org.lwjgl.opengl GL GL11]
           [org.lwjgl.system MemoryUtil]))

(set! *warn-on-reflection* true)

(defn get-display-scale [window]
  (let [x (make-array Float/TYPE 1)
        y (make-array Float/TYPE 1)]
    (GLFW/glfwGetWindowContentScale window x y)
    [(first x) (first y)]))

(defn -main [& args]
  (.set (GLFWErrorCallback/createPrint System/err))

  (GLFW/glfwInit)
  (GLFW/glfwWindowHint GLFW/GLFW_VISIBLE GLFW/GLFW_FALSE)
  (GLFW/glfwWindowHint GLFW/GLFW_RESIZABLE GLFW/GLFW_TRUE)
  (GLFW/glfwWindowHint GLFW/GLFW_DECORATED GLFW/GLFW_FALSE)
  (GLFW/glfwWindowHint GLFW/GLFW_TRANSPARENT_FRAMEBUFFER GLFW/GLFW_TRUE)

  (let [width 1200
        height 900
        window (GLFW/glfwCreateWindow width height "Carbon" MemoryUtil/NULL MemoryUtil/NULL)]
    (GLFW/glfwMakeContextCurrent window)
    (GLFW/glfwSwapInterval 1)
    (GLFW/glfwShowWindow window)
    (GL/createCapabilities)

    (doto (Thread. #(clojure.main/main))
      (.start))

    (repl/start-nrepl)
    (println "nREPL server started at locahost:7888")

    (let [context (DirectContext/makeGL)
          fb-id   (GL11/glGetInteger 0x8CA6)
          [scale-x scale-y] (get-display-scale window)
          target  (BackendRenderTarget/makeGL (* scale-x width) (* scale-y height) 0 8 fb-id FramebufferFormat/GR_GL_RGBA8)
          surface (Surface/makeFromBackendRenderTarget context target SurfaceOrigin/BOTTOM_LEFT SurfaceColorFormat/RGBA_8888 (ColorSpace/getSRGB))
          canvas  (.getCanvas surface)]

      (GLFW/glfwSetKeyCallback window (proxy [GLFWKeyCallback] []
                                        (invoke [window key scancode action mods]
                                          (#'editor/handle-key-event window key scancode action mods))))

      (GLFW/glfwSetCharCallback window (proxy [GLFWCharCallback] []
                                         (invoke [window codepoint]
                                           (#'editor/handle-char-event window codepoint))))

      (.scale canvas scale-x scale-y)

      (let [cmd-options (:options (cli/parse args))]
        (prn "parsed: " cmd-options)
        (when (:config cmd-options)
          (load-file (:config cmd-options))))

      (loop []
        (when (not (GLFW/glfwWindowShouldClose window))
          (let [layer (.save canvas)]
            (#'editor/render canvas 1200 900)
            (.restoreToCount canvas layer))
          (.flush context)
          (GLFW/glfwSwapBuffers window)
          (GLFW/glfwPollEvents)
          (recur))))

    (Callbacks/glfwFreeCallbacks window)
    (GLFW/glfwHideWindow window)
    (GLFW/glfwDestroyWindow window)
    (GLFW/glfwPollEvents)

    (GLFW/glfwTerminate)
    (.free (GLFW/glfwSetErrorCallback nil))
    (shutdown-agents)))

(comment

  69)
