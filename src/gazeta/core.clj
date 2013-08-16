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
  (let [f (fn [{:keys [error topic message]}] (callback error topic message))]
    (sub! :errors f)
    f))

(defn sub-errors! [topic callback]
  {:pre [(ifn? callback)]}
  (let [f (fn [{:keys [error topic message]}] (callback error topic message))]
    (sub! (error-channel-name topic) f)
    f))

(defn unsub! [topic callback]
  {:pre [(keyword? topic) (ifn? callback)]}
  (swap! callbacks #(update-in % [topic] (partial remove #{callback})))
  topic)

(defn unsub-all-errors! [callback]
  {:pre [(ifn? callback)]}
  (unsub! :errors callback))

(defn unsub-errors! [topic callback]
  {:pre [(ifn? callback)]}
  (unsub! (error-channel-name topic) callback))

(go
  (while true
    (let [[topic message] (<! incoming)]
      (doseq [cb (get @callbacks topic)]
        (go
          (try+
            (cb message)
            (catch Object error
              (pub-error! topic error message))))))))
