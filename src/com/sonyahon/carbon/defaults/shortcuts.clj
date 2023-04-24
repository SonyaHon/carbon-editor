(ns com.sonyahon.carbon.defaults.shortcuts
  (:require [com.sonyahon.carbon.editor.kbd :as kbd]
            [com.sonyahon.carbon.editor.state :as editor-state]
            [com.sonyahon.carbon.buffer.core :as buffer]))

(defn setup []
  (kbd/set-shortcut "backspace" #(editor-state/update-current-buffer!
                                   buffer/remove-from-point-backwards 1))

  (kbd/set-shortcut "C-b" #(editor-state/update-current-buffer!
                             buffer/move-point :x -1))
  (kbd/set-shortcut "C-f" #(editor-state/update-current-buffer!
                             buffer/move-point :x 1))
  (kbd/set-shortcut "C-p" #(editor-state/update-current-buffer!
                             buffer/move-point :y -1))
  (kbd/set-shortcut "C-n" #(editor-state/update-current-buffer!
                             buffer/move-point :y 1))
  (kbd/set-shortcut "return" #(editor-state/update-current-buffer!
                                buffer/insert-at-point "\n"))
  (kbd/set-shortcut "C-e" #(editor-state/update-current-buffer!
                             buffer/move-point :x 1000000000))
  (kbd/set-shortcut "C-a" #(editor-state/update-current-buffer!
                             buffer/move-point :x -1000000000)))