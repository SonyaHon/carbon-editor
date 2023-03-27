(ns com.sonyahon.carbon.repl
  (:require [nrepl.server :as nrepl]
            [cider.nrepl  :as cider]))

(defn start-nrepl []
  (nrepl/start-server :port 7888 :handler cider/cider-nrepl-handler))
