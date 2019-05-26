(defproject metabase/http-driver "1.0.0"
  :min-lein-version "2.5.0"

  :dependencies
  [[com.jayway.jsonpath/json-path "2.3.0"]]

  :jvm-opts
  ["-XX:+IgnoreUnrecognizedVMOptions"
   "--add-modules=java.xml.bind"]

  :profiles
  {:provided
   {:dependencies
    [[org.clojure/clojure "1.9.0"]
     [metabase-core "1.0.0-SNAPSHOT"]]}

   :uberjar
   {:auto-clean    true
    :aot :all
    :omit-source true
    :javac-options ["-target" "1.8", "-source" "1.8"]
    :target-path   "target/%s"
    :uberjar-name  "http.metabase-driver.jar"}})
