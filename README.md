# HTTP Metabase Driver

This is a proof of concept HTTP "driver" for [Metabase](https://metabase.com/).

Previous discussion: https://github.com/metabase/metabase/pull/7047

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
