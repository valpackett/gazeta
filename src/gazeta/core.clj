(ns gazeta.core
  (:use [clojure.core async]
        [slingshot slingshot]))

(def incoming (chan))
(def callbacks (atom {}))

(defn- error-channel-name [topic]
  (keyword (str "errors-" (name topic))))

(defn pub! [topic message]
  (>!! incoming [topic message])
  topic)

(defn pub-error! [topic error message]
  (let [data {:error error
              :topic topic
              :message message}]
    (pub! :errors data)
    (pub! (error-channel-name topic) data)))

(defn sub! [topic callback]
  {:pre [(keyword? topic) (ifn? callback)]}
  (swap! callbacks #(update-in % [topic] conj callback))
  topic)

(defn sub-all-errors! [callback]
  {:pre [(ifn? callback)]}
  (sub! :errors
        (fn [{:keys [error topic message]}] (callback error topic message))))

(defn sub-errors! [topic callback]
  {:pre [(ifn? callback)]}
  (sub! (error-channel-name topic)
        (fn [{:keys [error topic message]}] (callback error topic message))))

(defn unsub! [topic callback]
  {:pre [(keyword? topic) (ifn? callback)]}
  (swap! callbacks #(update-in % [topic] (partial remove #{callback})))
  topic)

(go
  (while true
    (let [[topic message] (<! incoming)]
      (doseq [cb (get @callbacks topic)]
        (go
          (try+
            (cb message)
            (catch Object error
              (pub-error! topic error message))))))))
