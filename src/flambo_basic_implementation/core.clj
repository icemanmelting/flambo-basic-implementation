(ns flambo-basic-implementation.core
  (:require [flambo.conf :as conf]
            [flambo.api :as f]
            [flambo.sql :as sql]
            [clojure.java.io :as io]
            [clojure.string :as s])
  (:import (org.apache.spark.sql.catalyst.expressions GenericRow))
  (:gen-class))

(def ^:private conf
  (-> (conf/spark-conf)
      (conf/master "local")
      (conf/app-name "flaming_test")))

(defn- from-csv-file [file]
  (sql/with-sql-context c conf (let [df (sql/read-csv c file :header true)]
                                 (sql/register-data-frame-as-table c df "cars")
                                 (let [ds (sql/sql c "SELECT * FROM cars WHERE make = 'Opel'")]
                                   (f/collect ds)))))

(defn- row-to-map [^GenericRow r]
  {:year (.getString r 0)
   :make (.getString r 1)
   :model (.getString r 2)
   :comment (.getString r 3)
   :blank (.getString r 3)})

(defn -main [& args]
  (let [results (from-csv-file (str (io/resource "data.csv")))]
    (prn (map row-to-map results))))
