(defproject apibot "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :plugins [[lein-cljfmt "0.5.6"]
            [io.aviso/pretty "0.1.32"]]
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[com.novemberain/monger "3.1.0"]
                 [criterium "0.4.4"]
                 [de.ubercode.clostache/clostache "1.4.0"]
                 [funcool/cats "2.0.0"]
                 [http-kit "2.2.0"]
                 [io.aviso/pretty "0.1.32"]
                 [mount "0.1.10"]
                 [org.mozilla/rhino "1.7.7.1"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [presto "0.2.0"]
                 [ring/ring-core "1.2.1"]
                 [ring/ring-jetty-adapter "1.2.1"]])

