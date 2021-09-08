(ns metabase.driver.http
  "HTTP API driver."
  (:require [cheshire.core :as json]
            [clojure.tools.logging :as log]
            [clojure.string :as string]
            [metabase.driver :as driver]
            [metabase.driver.http.query-processor :as http.qp]
            [metabase.driver.common.parameters.values :as common.qp.params]
            [metabase.query-processor.store :as qp.store]))

(defn find-first
  [f coll]
  (first (filter f coll)))

(defn- database->definitions
  [database]
  (json/parse-string (:definitions (:details database)) keyword))

(defn- database->table-defs
  [database]
  (or (:tables (database->definitions database)) []))

(defn- database->table-def
  [database name]
  (log/info "database->table-def database:" database ", name: " name)
  (first (filter #(= (:name %) name) (database->table-defs database))))

(defn table-def->field
  [table-def name]
  (log/info "table-def->field table-def: " table-def "name" name)
  (find-first #(= (:name %) name) (:fields table-def)))

(defn mbql-field->expression
  [table-def expr]
  (log/info "mbql-field->expression table-def: " table-def "expr" expr)
  (let [field-store (qp.store/field (get expr 1))]
    (:name field-store)))

(defn mbql-aggregation->aggregation
  [_table-def mbql-aggregation]
  (log/info "mbql-aggregation->aggregation" mbql-aggregation)
  [(:name (get mbql-aggregation 2))])

(def json-type->base-type
  {:string  :type/Text
   :number  :type/Float
   :boolean :type/Boolean})

(driver/register! :http)

(defmethod driver/supports? [:http :basic-aggregations]
  [_driver _feature]
  true)

(defmethod driver/supports? [:http :native-parameters]
  [_driver _feature]
  true)

(defmethod driver/can-connect? :http [_ _]
  true)

(defmethod driver/substitute-native-parameters :http
  [_driver inner-query]
  (log/info "inner-query" inner-query)
  (let [param->value (common.qp.params/query->params-map inner-query)
        replaced-query (reduce-kv (fn [m k v] (string/replace m (str "{{" k "}}") v))
                                  (:query inner-query)
                                  param->value)]
    (assoc inner-query :query replaced-query)))

(defmethod driver/describe-database :http [_ database]
  (let [table-defs (database->table-defs database)]
    (log/info "Describe database...")
    {:tables (set (for [table-def table-defs]
                    {:name   (:name table-def)
                     :schema (:schema table-def)}))}))

(defmethod driver/describe-table :http [_ database table]
  (let [table-def  (database->table-def database (:name table))]
    (log/info "Describe table...")
    {:name   (:name table-def)
     :schema (:schema table-def)
     :fields (set (for [field (:fields table-def)]
                    {:name          (:name field)
                     :database-type (:type field)
                     :database-position (:id database)
                     :base-type     (or (:base_type field)
                                        (json-type->base-type (keyword (:type field))))}))}))

(defmethod driver/mbql->native :http [_ query]
  (let [database    (qp.store/database)
        table       (qp.store/table (:source-table (:query query)))
        table-def   (database->table-def database (:name table))
        breakout    (map (partial mbql-field->expression table-def) (:breakout (:query query)))
        aggregation (map (partial mbql-aggregation->aggregation table-def) (:aggregation (:query query)))]
    (log/info "driver/mbql->native Query:" query)
    (log/info "table" table)
    (log/info "breakout" breakout)
    (log/info "aggregation" aggregation)
    {:query (merge (select-keys table-def [:method :url :headers :body])
                   {:result (merge (:result table-def)
                                   {:breakout     breakout
                                    :aggregation  aggregation})})
     :mbql? true}))

(defmethod driver/execute-reducible-query :http
  [driver {native :native :as query} context respond]
  (log/info "Driver: " driver)
  (log/info "Query: " query)
  (log/info "Native:" native)
  (log/info "Context " context)
  (http.qp/execute-http-request-reducible respond native))

(comment
  ; You can use this to debug / test the driver

  (http.qp/execute-http-request-reducible
   println
   {:query {:url "https://api.coindesk.com/v1/bpi/currentprice.json"}})

  (http.qp/execute-http-request-reducible
   println
   {:query {:url "https://pokeapi.co/api/v2/pokemon?limit=100&offset=200"
            :result {:path "$.results"
                     :fields ["$.name"]}}})

  (http.qp/execute-http-request-reducible
   println
   {:query {:url "https://beta.pokeapi.co/graphql/v1beta"
            :method "POST"
            :headers {:Accept "application/json"
                      :Content-Type "application/json"}
            :result {:path "$.data.pokemon_v2_item"
                     :fields ["$.name" "$.cost"]}
            :body {:query "query getItems{pokemon_v2_item{name,cost}}"
                   :variables nil
                   :operationName "getItems"}}}))
