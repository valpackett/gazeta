# gazeta

A publish-subscribe (PubSub) framework for Clojure, based on core.async.  
Has Lamina and RxJava integration.

## Usage

### Basics

```clojure
(ns app
  (:use gazeta.core))

(sub! :posts
  (fn [{:keys [user text]}]
    (println (str user " posted: " text))))

(pub! :posts {:user "myfreeweb", :text "PubSub is magic!"})

;;;; Asynchronously printed to console:
; myfreeweb posted: PubSub is magic!
```

You can publish before there are any subscribers -- in that case, the message will be lost.  
The subscriber function is executed in a core.async `go` block.

### Error handling

When an exception is thrown in a subscriber function, a map with `:error`, `:topic` and `:message` is published to `:errors` and `:errors-{{topic}}`.  
There are shorthand functions `sub-all-errors!` and `sub-errors!` to subscribe.  
They accept functions that take the error, the topic and the message as separate args.

```clojure
(ns app
  (:use gazeta.core))

(sub-all-errors!
  (fn [error topic message]
    (println (str error " happened with message " message " on topic " topic))))

;; Same as:
; (sub! :errors (fn [{:keys [error topic message]}] ...))

(sub-errors! :actions
  (fn [error topic message]
    (println (str error " happened with message " message " on topic :actions"))))

;; Same as:
; (sub! :errors-actions (fn [{:keys [error topic message]}] ...))

(sub! :actions
  (fn [x] (throw (Exception. "I am an error!"))))

(sub! :actions
  (fn [x] (println (str x " happened!"))))

(pub! :actions "something")

;;;; Asynchronously printed to console:
; something happened!
; java.lang.Exception: I am an error! happened with message something on topic :actions
; java.lang.Exception: I am an error! happened with message something on topic :actions
```

`try+` from [slingshot](https://github.com/scgilardi/slingshot) is used, so any object can be caught.  
If you want to use slingshot's advanced matching though, use `try+` explicitly in the subscriber :-)

### Unsubscribing

Just pass the same args to `unsub!`:

```clojure
(let [cb (fn [msg] (println (str "Message: " msg)))]
  (sub! :messages cb)
  (pub! :messages "Hello!")
  (unsub! :messages cb))
```

### Chains

Interesingly, `pub!`, `sub!`, `unsub!` and `sub-errors!` return the topic name, so you can chain them with `->`:

```clojure
(-> :thingy
    (sub-errors! (fn [err topic msg] (prn err topic msg)))
    (sub! println)
    (pub! "test one")
    (pub! "test two"))

;;;; Asynchronously printed to console:
; test one
; test two
```

You'll usually use `pub!` and `sub!` in separate functions. Often even in separate namespaces.

But the topic name is the only thing that makes sense as a return value :-)

### Lamina integration

You can pipe gazeta topics with lamina channels using `pub-lamina-channel!` and `sub-lamina-channel!` from `gazeta.lamina` and use `pub-on-realized!` to publish when async-promises are realized:

```clojure
(ns app
  (:use [gazeta core lamina])
  (:require [lamina.core :as lamina]
            [lamina.executor :as executor]))

(sub! :from-lamina (fn [r] (println (str "From lamina: " r))))
(def lamina-publisher (lamina/channel))
(pub-lamina-channel! :from-lamina lamina-publisher)
(lamina/enqueue lamina-publisher 1)
(lamina/enqueue lamina-publisher 2)

;;;; Asynchronously printed to console:
; From lamina: 1
; From lamina: 2


(def lamina-receiver (lamina/channel))
(sub-lamina-channel! :messages lamina-receiver)
(pub! :messages "hello")
(lamina/read-channel lamina-receiver)

; "hello"

(def lamina-receiver-2 (lamina-channel-for-topic :messages))
(pub! :messages "hello")
(lamina/read-channel lamina-receiver-2)

; "hello"


(sub! :results (fn [r] (println (str "Result: " r))))
(pub-on-realized! :results (executor/task (+ 1 2)))

;;;; Asynchronously printed to console:
; Result: 3
```

*Note:* gazeta does not depend on lamina.

### RxJava integration

You can subscribe to topics as Observables using `observable-for-topic` and pipe Observables into topics using `observable-to-topic!` from `gazeta.rx`:

```clojure
(ns app
  (:use [gazeta core rx])
  (:import rx.Observable))

(sub! :from-rx (fn [msg] (println (str "From rx: " msg))))
(observable-to-topic! :from-rx (Observable/from ["hello" "world"]))

;;;; Asynchronously printed to console:
; From rx: world
; From rx: hello

(-> (observable-for-topic :to-rx)
    (.subscribe (fn [msg] (println (str "To rx: " msg)))))
(pub! :to-rx "hi")

;;;; Asynchronously printed to console:
; To rx: hi
```

*Note:* gazeta does not depend on rxjava-core nor rxjava-clojure.

Gazeta also exposes RxJava integration as a class to use from other JVM languages.
Here's a Scala example:

```scala
import gazeta.RxGazeta
import rx.Observable

RxGazeta.observableForTopic("to-scala")
  .subscribe((message: String) => {
    println("Scala got: " + message)
    RxGazeta.observableToTopic("from-scala", Observable.just("Got a message"))
  })
```
