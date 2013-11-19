(ns foursquare-cli.core
  (:gen-class ))

(require '[clojure.data.json :as json])
(use '[clojure.string :only [join, trim, split]])

(def fsq-credentials
  {
    "client_id" "TY1ARBVSEIZHZVAZZRHIEQNDJS0FMWIBN0UBL1SBNJQB5Z1Z"
    "client_secret" "BHMPRXUFOU2K2GI4BO5URAQW350L5LJ5P2A2Q3WQBMRNGOHL"
    "v" "20131119"
    })

(defn hash-to-path [hash]
  (join "&"
    (map (fn [e] (join "=" [(key e) (val e)])) hash)))

(defn query-foursquare [endpoint query]
  (str "https://api.foursquare.com/v2/" endpoint "?"
    (hash-to-path fsq-credentials) "&" (hash-to-path query)))

(defn cmd-get [args]
  (slurp (last args)))

(defn cmd-locate [& args]
  (json/read-str (slurp "http://www.telize.com/geoip")))

(defn cmd-trends [args]
  (let [location (cmd-locate)
        limit (or (last args) 3)
        ll (join "," [(location "latitude") (location "longitude")])
        url (query-foursquare "venues/explore" {"limit" limit, "ll" ll})]
    (json/read-str (slurp url))))

(def commands
  {"get" cmd-get, "locate" cmd-locate, "trends" cmd-trends})

(defn do-command [command args]
  (println
    (if (contains? commands command)
      ((commands command) args)
      (str "Available commands: " (join ", " (keys commands))))))

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
