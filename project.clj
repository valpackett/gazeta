(defproject gazeta "0.1.0-SNAPSHOT"
  :description "Publish-subscribe (pubsub) framework for Clojure"
  :url "https://github.com/myfreeweb/gazeta"
  :license {:name "WTFPL"
            :url "http://www.wtfpl.net/about/"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.async "0.1.0-SNAPSHOT"]
                 [slingshot "0.10.3"]]
  :profiles {:dev {:dependencies [[midje "1.5.0"]
                                  [lein-release "1.0.0"]
                                  [lamina "0.5.0-rc3"]
                                  [com.netflix.rxjava/rxjava-clojure "0.9.0"]]}}
  :plugins [[lein-midje "3.0.0"]
            [lein-release "1.0.0"]]
  :bootclasspath true
  :lein-release {:deploy-via :lein-deploy}
  :repositories [["snapshots" {:url "https://clojars.org/repo" :creds :gpg}]
                 ["releases"  {:url "https://clojars.org/repo" :creds :gpg}]
                 ["sonatype-oss-public" {:url "https://oss.sonatype.org/content/groups/public/"}]])
