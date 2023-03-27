(ns com.sonyahon.carbon.cli
  (:require [clojure.tools.cli :as cli]))

(def cli-options
  [["-c" "--config PATH" "Entry to the configuration file"
    :default nil]])

(defn parse
  "Parse CLI options to Carbon and return a map of all entries"
  [args]
  (cli/parse-opts args cli-options))
