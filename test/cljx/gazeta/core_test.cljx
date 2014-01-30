(ns gazeta.core-test
  (:require [gazeta.core :as gazeta :refer [pub! sub!]]
            ;; core.async
      #+clj [clojure.core.async :refer [<! go]]
     #+cljs [cljs.core.async :refer [<!]]
            ;; test
      #+clj [clojure.test :as t :refer (is deftest testing)]
     #+cljs [cemerick.cljs.test :as t])
  #+cljs (:require-macros ;; core.async
                          [cljs.core.async.macros :refer [go]]
                          ;; test
                          [cemerick.cljs.test :refer (is deftest testing)]))

(deftest sub!-test
  (testing "Test the sub! function\n"
    (let [topic :posts
          msg "PubSub is magic!"
          callback (fn [] msg)]
      (sub! topic callback)
      (is (= 1 (count @gazeta/callbacks)))
      (is (= callback (first (get @gazeta/callbacks topic))))
      (is (= msg ((first (get @gazeta/callbacks topic))))))))

(deftest pub!-test
  (testing "Test the pub! function\n"
    (let [topic :posts
          msg "PubSub is magic!"
          status (atom "fail")
          expected msg]
      (sub! topic (fn [message] (reset! status message)))
      (pub! topic msg)
      #+clj (let [f (future (Thread/sleep 5) @status)]
              (is (= expected @f)))
      ;; HowTo: Test async with clojurescript.test?
      ;; Hope: 2014-01-23 -> https://github.com/cemerick/clojurescript.test/issues/34#issuecomment-33063534
      #+cljs (is (not= expected @status))
      )))