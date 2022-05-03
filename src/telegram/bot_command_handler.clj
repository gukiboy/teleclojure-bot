(ns telegram.bot-command-handler
  (:require [clojure.edn :as edn]
            [morse.handlers :as h]
            [clojure.java.io :as io]
            [morse.api :as t]
            [morse.polling :as p]
            [io.pedestal.log :as log]
            [clojure.string :as str]
            [com.stuartsierra.component :as component]))

(defn format-response
  [form evaluation]
  (print-str "```; eval:" form "```\n"
             "`" evaluation "`"))


(defrecord TelegramBot [token]
  component/Lifecycle
  (start [this]
    (h/defhandler teleclojure-bot
        (h/command-fn "start" (fn [{{id :id :as chat} :chat}]
                                (println "Bot joined new chat" chat)))
        (h/command-fn "eval" (fn [{:keys [chat text] :as msg}]
                               (let [id (:id chat)
                                     form (subs text 6)]
                                 (log/info :form-evaluation-request {:form form})
                                 (try
                                   (prn :FORME form)
                                   (let [pbform (-> form .getBytes io/reader java.io.PushbackReader.)
                                         evaluation (edn/read pbform)
                                         message-content (format-response form evaluation)]
                                     (prn :evaluation (eval evaluation))
                                     (log/info :form-evaluation {:form form
                                                                 :evaluation evaluation})
                                     (t/send-text token id {:parse_mode "Markdown"} (str "✅\n" message-content)))
                                   (catch Exception ex
                                     (do (log/error :error ex)
                                         (t/send-text token id {:parse_mode "Markdown"}
                                                      (str "❌\n" (format-response form (:cause (Throwable->map ex))))))))))))
    (let [session (p/start token teleclojure-bot)]
      (log/info :start-telegram-bot {:session session})
      (assoc this :teleclojure-bot session)))
  (stop [this]
    (let [session (some-> this :teleclojure-bot)]
      (log/info :stop-telegram-bot {:session session})
      (when session (p/stop session))
      (assoc this :teleclojure-bot nil))))

(defn new-telegram-bot [config]
  (map->TelegramBot config))


(comment
  (def sesh (p/start token teleclojure-bot))
  (p/stop sesh)

  (read-string "\"tewse\"")
  (read-string "{:banana “teste” :maca “fruta”}")

  (try (load-string ")")
       (catch Exception ex (:cause (Throwable->map ex)))))
