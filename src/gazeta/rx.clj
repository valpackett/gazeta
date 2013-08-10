(ns gazeta.rx
  (:use gazeta.core)
  (:import rx.Observable rx.subscriptions.Subscriptions)
  (:gen-class
    :name gazeta.RxGazeta
    :methods [#^{:static true} [observableForTopic String]
              #^{:static true} [observableToTopic String Observable]]))

(defn observable-for-topic [topic]
  (Observable/create
    (fn [observer]
      (let [cb (fn [message] (.onNext observer message))]
        (sub! topic cb)
        (Subscriptions/create #(unsub! topic cb))))))

(defn -observableForTopic [topic]
  (observable-for-topic (keyword topic)))

(defn observable-to-topic! [topic observable]
  (.subscribe observable (partial pub! topic)))

(defn -observableToTopic [topic observable]
  (observable-to-topic! (keyword topic) observable))
