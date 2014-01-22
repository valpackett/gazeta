(defproject gazeta "0.2.0"
  :description "Publish-subscribe (pubsub) framework for Clojure and ClojureScript"
  :url "https://github.com/myfreeweb/gazeta"
  :license {:name "WTFPL"
            :url "http://www.wtfpl.net/about/"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2138"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [slingshot "0.10.3"]]
  :profiles {:dev {:dependencies [[lein-release "1.0.0"]
                                  [com.keminglabs/cljx "0.3.2"]
                                  [prismatic/cljs-test "0.0.6"]
                                  [lamina "0.5.0-rc3"]
                                  [com.netflix.rxjava/rxjava-clojure "0.9.0"]]}}
;                   :plugins [[com.cemerick/austin "0.1.3"]]
;                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl
;                                                     cljx.repl-middleware/wrap-cljx]}}}
  :plugins [[lein-release "1.0.0"]
            [lein-cljsbuild "1.0.0-alpha2"]
            [com.keminglabs/cljx "0.3.2"]]
  :cljx {:builds [{:source-paths ["src/cljx"]
                   :output-path "target/generated/src/clj"
                   :rules :clj}
                  {:source-paths ["src/cljx"]
                   :output-path "target/generated/src/cljs"
                   :rules :cljs}
                  {:source-paths ["test/cljx"]
                   :output-path "target/generated/test/clj"
                   :rules :clj}
                  {:source-paths ["test/cljx"]
                   :output-path "target/generated/test/cljs"
                   :rules :cljs}]}
  :prep-tasks ["cljx" "javac" "compile"]
  :source-paths ["target/generated/src/clj" "src/clj"]
  :resource-paths ["target/generated/src/cljs"]
  :test-paths ["target/generated/test/clj" "test/clj"]
  :cljsbuild {:builds
              {:dev {:source-paths ["src/clj" "target/generated/src/cljs"]
                     :compiler {:output-to "target/main.js"
                                :optimizations :whitespace
                                :pretty-print true}}
               :test {:source-paths [ "src/clj" "test/clj"
                                      "target/generated/src/cljs" "target/generated/test/cljs"]
                      :compiler {:output-to "target/unit-test.js"
                                 :optimizations :whitespace
                                 :pretty-print true}}}}
  :bootclasspath true
  :lein-release {:deploy-via :lein-deploy}
  :repositories [["snapshots" {:url "https://clojars.org/repo" :creds :gpg}]
                 ["releases"  {:url "https://clojars.org/repo" :creds :gpg}]
                 ["sonatype-oss-public" {:url "https://oss.sonatype.org/content/groups/public/"}]])
