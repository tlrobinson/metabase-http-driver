# HTTP Metabase Driver

This is a proof-of-concept HTTP "driver" for [Metabase](https://metabase.com/).

Previous discussion: https://github.com/metabase/metabase/pull/7047

## Usage

Currently the simplest "native" query for this driver is simply an object with a `url` property:

```json
{ "url": "https://api.coinmarketcap.com/v1/ticker/" }
```

The driver will make a `GET` request and parse the resulting JSON array into rows. Currently it only supports JSON.

You can provide a different `method` as well as `headers` and a JSON `body`:

```json
{
  "url": "https://api.coinmarketcap.com/v1/ticker/",
  "method": "POST",
  "headers": {
    "Authentication": "SOMETOKEN"
  },
  "body": {
    "foo": "bar"
  }
}
```

Additionally, you can provide a `result` object with a JSONPath to the "root" in the response, and/or a list of `fields`:

```json
{
  "url" : "https://blockchain.info/blocks?format=json",
  "result" : {
    "path" : "blocks",
    "fields": ["height", "time"]
  }
}
```

You can also predefine "tables" in the database configuration's `Table Definitions` setting. These tables will appear in the graphical query builder:

```json
{
   "tables" : [
      {
         "name" : "Blocks",
         "url" : "https://blockchain.info/blocks?format=json",
         "fields" : [
            { "name" : "height", "type" : "number" },
            { "name" : "hash", "type" : "string" },
            { "name" : "time", "type" : "number" },
            { "type" : "boolean", "name" : "main_chain" }
         ],
         "result" : {
            "path" : "blocks"
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
cd /path/to/metabase_source
lein install-for-building-drivers
```

### Build the HTTP driver

```bash
# (In the HTTP driver directory)
lein clean
DEBUG=1 LEIN_SNAPSHOTS_IN_RELEASE=true lein uberjar
```

### Copy it to your plugins dir and restart Metabase

```bash
mkdir -p /path/to/metabase/plugins/
cp target/uberjar/http.metabase-driver.jar /path/to/metabase/plugins/
jar -jar /path/to/metabase/metabase.jar
```

_or:_

```bash
mkdir -p /path/to/metabase_source/plugins
cp target/uberjar/http.metabase-driver.jar /path/to/metabase_source/plugins/
cd /path/to/metabase_source
lein run
```

