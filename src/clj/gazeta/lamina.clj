(ns gazeta.lamina
  (:use gazeta.core
        [clojure.core async]
        [lamina core executor]))

(defn pub-on-realized! [topic ap]
  (on-realized ap
               #(pub! topic %)
               #(pub-error! topic % nil)))

(defn pub-lamina-channel! [topic ch]
  (receive-all ch (partial pub! topic)))

(defn sub-lamina-channel! [topic ch]
  (sub! topic (fn [message] (enqueue ch message))))

(defn lamina-channel-for-topic [topic]
  (let [ch (channel)]
    (sub-lamina-channel! topic ch)
    ch))
