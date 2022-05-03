(ns teleclojure.core
  (:require [telegram.bot-command-handler :as bot]
            [aero.core :refer [read-config]]
            [clojure.java.io :as io]
            [com.stuartsierra.component :as component]))

(defn config
  ([]
   (config (or (keyword (System/getenv "PROFILE")) :local)))
  ([env]
   (read-config (io/resource "config.edn") {:profile env})))

(defn base-system []
  (let [profile (or (keyword (System/getenv "ENV")) :local)
        config (config profile)]
    (component/system-map
      :telegram-bot (bot/new-telegram-bot (:telegram-bot config)))))

(defonce system (base-system))

(comment
  ;; Evaluate the do block below to start the service. reevaluate it to
  ;; restart it quickly. Whenever you change a file, reload the changed file
  ;; and then reevaluate this block
  (do (load-file "src/teleclojure/core.clj")
      (alter-var-root #'system component/stop)
      (alter-var-root #'system (constantly (base-system)))
      (alter-var-root #'system component/start)
      nil)
  )
