(ns core
  (:require [morse.handlers :as h]
            [morse.api :as t]
            [morse.polling :as p]
            [io.pedestal.log :as log]
            [clojure.string :as str]))

(def ^:private token "")

(defn answer-inline
  [{:keys [title message_text input_message_content]}]
  [{:id :form-eval-1
    :type :article
    :title title
    :message_text message_text
    :input_message_content {:message_text input_message_content
                            :parse_mode :MarkdownV2}}])

(defn format-response
  [form evaluation]
  (print-str "```" form "```\n"
             "`" evaluation "`"))

(h/defhandler teleclojure-bot
  (h/command-fn "start" (fn [{{id :id :as chat} :chat}]
                          (println "Bot joined new chat" chat)))
  (h/command "help" {{id :id :as chat} :chat}
             (println "Help was requested in " chat)
             (t/send-text token id "Help is on the way"))
  (h/command-fn "eval" (fn [{:keys [chat text] :as msg}]
                         (let [id (:id chat)
                               form (subs text 6)]
                           (log/info :form-evaluation-request {:form form})
                           (try
                             (let [evaluation (load-string form)
                                   message-content (format-response form evaluation)]
                               (log/info :form-evaluation {:form form
                                                           :evaluation evaluation})
                               (t/send-text token id (format-response form evaluation)))
                             (catch Exception ex
                               (do (log/error :error ex)
                                   (t/send-text token id (format-response form (:cause (Throwable->map ex)))))))))))

(comment
  (def sesh (p/start token teleclojure-bot))
  (p/stop sesh)

  (read-string "\"tewse\"")
  (read-string "{:banana “teste” :maca “fruta”}")

  (try (load-string ")")
       (catch Exception ex (:cause (Throwable->map ex)))))

