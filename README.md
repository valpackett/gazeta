# gazeta

A publish-subscribe (PubSub) framework for Clojure, based on core.async.

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
  (fn [error topic message] (println (str error " happened with message " message " on topic " topic))))

;; Same as:
; (sub! :errors (fn [{:keys [error topic message]}] ...))

(sub-errors! :actions
  (fn [error topic message] (println (str error " happened with message " message " on topic :actions"))))

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
