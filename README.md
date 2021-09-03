# HTTP Metabase Driver

This is a proof-of-concept HTTP "driver" for [Metabase](https://metabase.com/).

Previous discussion: https://github.com/metabase/metabase/pull/7047

## Usage

Currently the simplest "native" query for this driver is simply an object with a `url` property:

```json
{ "url": "https://api.coindesk.com/v1/bpi/currentprice.json" }
```

The driver will make a `GET` request and parse the resulting JSON array into rows. Currently it only supports JSON.

You can provide a different `method` as well as `headers` and a JSON `body`:

```json
{
  "url": "https://beta.pokeapi.co/graphql/v1beta",
  "method": "POST",
  "headers": {
    "Accept": "application/json",
    "Content-Type": "application/json"
  },
  "result": {
    "path": "$.data.pokemon_v2_item",
    "fields": [
      "$.name",
      "$.cost"
    ]
  },
  "body": {
    "query": "query getItems{pokemon_v2_item{name,cost}}",
    "variables": null,
    "operationName": "getItems"
  }
}
```

Additionally, you can provide a `result` object with a JSONPath to the "root" in the response, and/or a list of `fields`:

```json
{
  "url" : "https://pokeapi.co/api/v2/pokemon?limit=100&offset=200",
  "result" : {
    "path" : "$.results",
    "fields": ["$.name"]
  }
}
```

You can also predefine "tables" in the database configuration's `Table Definitions` setting. These tables will appear in the graphical query builder:

```json
{
   "tables" : [
      {
         "name" : "Ability",
         "url" : "https://pokeapi.co/api/v2/ability/?limit=20&offset=20",
         "fields" : [
            { "name" : "$.name", "type" : "string" },
            { "name" : "$.url", "type" : "string" },
            { "name" : "$.cost", "type" : "number" }
         ],
         "result" : {
            "path" : "$.results"
         }
      }
   ]
}
```

There is limited support for aggregations and breakouts, but this is very experimental and may be removed in future versions.

## Building the driver

### Prereq: Install Metabase as a local maven dependency, compiled for building drivers

Clone the [Metabase repo](https://github.com/metabase/metabase) first if you haven't already done so.
```bash
# clone metabase repository
git clone --depth=1 -b master https://github.com/metabase/metabase.git metabase

# build metabase jars
./prepare-metabase.sh
```

### Build the HTTP driver
```bash
clj -X:build :project-dir \"${PWD}\"
```

This will create `target/http.metabase-driver.jar` Copy this file to `/path/to/metabase/plugins/` and restart your server, and the driver will show up.
