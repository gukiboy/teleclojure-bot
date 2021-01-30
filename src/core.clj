(ns core
  (:require [morse.handlers :as h]
            [morse.api :as t]
            [morse.polling :as p]))

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
  (h/inline-fn (fn [inline] 
                 (try (let [form (:query inline)
                            evaluation (load-string form)
                            message-content (format-response form evaluation)
                            inline-response (answer-inline {:title form
                                                            :message_text (print-str evaluation)
                                                            :input_message_content message-content})]
                        (clojure.pprint/pprint form)
                        (clojure.pprint/pprint inline-response)
                        (clojure.pprint/pprint (t/answer-inline token (:id inline) (:options inline) inline-response)))
                      (catch RuntimeException rte
                        (t/answer-inline token (:id inline) (:options inline)
                                         (let [form (:query inline)
                                               title form
                                               cause (:cause (Throwable->map rte))]
                                           (clojure.pprint/pprint (answer-inline {:title title                                                           :message_text cause
                                                           :input_message_content (format-response title cause)})))))
                      (catch Exception ex (.printStackTrace ex))))))

(comment
  (def sesh (p/start token teleclojure-bot))
  (p/stop sesh)

  (try (load-string ")")
       (catch Exception ex (:cause (Throwable->map ex)))))

