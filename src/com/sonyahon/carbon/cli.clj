(ns com.sonyahon.carbon.cli
  (:require [clojure.tools.cli :as cli]))

(def cli-options
  [["-h" "--help" "Displays help message"]
   ["-wh" "--window-height PIXELS" "Sets initial window height" :default 800]
   ["-ww" "--window-width PIXELS" "Sets initial window width" :default 1200]])

(defn parse
  "Parse CLI options to Carbon and return a map of all entries"
  [args]
  (cli/parse-opts args cli-options))
