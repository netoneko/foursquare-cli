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
  (let [location (json/read-str (slurp "http://www.telize.com/geoip"))]
    [(location "latitude") (location "longitude")]))

(defn cmd-locate-osm [args]
  (let [name (java.net.URLEncoder/encode (join " " args))
        url (str "http://nominatim.openstreetmap.org/search.php?format=json&q=" name)
        location (first (json/read-str (slurp url)))]
    (println url)
    [(location "lat") (location "lon")]))

(defn cmd-foursquare [command args]
  (let [location (if (zero? (count args)) (cmd-locate) (cmd-locate-osm args))
        options {"limit" 10, "radius" 1000, "ll" (join "," location)}
        url (query-foursquare (str "venues/" command) options)]
    (println url)
    (json/read-str (slurp url))))

(defn print-venue [venue]
  (let [name (venue "name")
        hours (venue "hours")
        location (venue "location")]
    (str "*****" \newline name
      (if (and (not (nil? hours)) (hours "isOpen")) " (open)" " (closed)") \newline
      (location "address") ", " (location "city"))))

(defn print-venue-group [group]
  (str (group "type") \newline
    (join \newline (map #(print-venue (%1 "venue")) (group "items")))))

(def commands
  {"get" cmd-get
   "locate" cmd-locate
   "trends" (fn [args] (let [results (cmd-foursquare "trending" args)]
                         (join \newline
                           (map print-venue ((results "response") "venues")))))
   "explore" (fn [args] (let [results (cmd-foursquare "explore" args)]
                          (join \newline
                            (map print-venue-group ((results "response") "groups")))))
   })

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
