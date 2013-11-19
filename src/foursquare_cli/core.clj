(ns foursquare-cli.core
  (:gen-class ))

(use '[clojure.string :only [join, trim, split]])

(def commands
  {
    "get" (fn [args]
            (println (slurp (last args))))
    })

(defn do-command [command args]
  (if (contains? commands command)
    ((commands command) args)
    (println "Nope")))

(defn read-eval-print-loop []
  (print "> ")
  (flush)
  (let [line (read-line)]
    (if (not (nil? line))
      (let [tokens (split line #"\s")
            command (first tokens)
            args (rest tokens)]
        (do-command command args)
        (recur)))))

(defn -main []
  (read-eval-print-loop))
