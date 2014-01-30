(ns gazeta.core
  #+clj (:require [clojure.core.async :refer [chan >!! <! go]]
                  [slingshot.slingshot :refer [try+]])
  #+cljs (:require [cljs.core.async :refer [chan put! <!]])
  #+cljs (:require-macros [cljs.core.async.macros :refer [go]]))

(def incoming (chan))
(def callbacks (atom {}))

(defn- error-channel-name [topic]
  (keyword (str "errors-" (name topic))))

(defn pub! [topic message]
  #+clj (>!! incoming [topic message])
  #+cljs (put! incoming [topic message])
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

(defn unsub-all-the-things! []
  (swap! callbacks (constantly {})))

(go
  (while true
    (let [[topic message] (<! incoming)]
      (doseq [cb (get @callbacks topic)]
        (go
          #+clj (try+
                 (cb message)
                 (catch Object error
                   (pub-error! topic error message)))
          #+cljs (try
                   (cb message)
                   (catch js/Error error
                     (pub-error! topic error message))))))))
