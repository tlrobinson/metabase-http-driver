# HTTP Metabase Driver

This is a proof-of-concept HTTP "driver" for [Metabase](https://metabase.com/).

Previous discussion: https://github.com/metabase/metabase/pull/7047

## Usage

### Within Native/SQL queries

> "SQL Query" refers to the screen within Metabase. This driver does not support the
> SQL programming langauge

Currently, the simplest native/SQL query for this driver is simply an object with a
`url` and `name` property which can be run within the view of a table:

```json
{
  "name": "pokemon",
  "url": "https://pokeapi.co/api/v2/pokemon"
}
```

The driver will make a `GET` request and parse the resulting JSON array into rows. Currently, it only supports JSON.

You can provide a different `method` as well as `headers` and a JSON `body`:

```json
{
  "url": "https://example.com",
  "method": "POST",
  "headers": {
    "Accept": "application/json",
    "Content-Type": "application/json"
  },
  "result": {
    "path": "$.data.pokemon_v2_item",
    "fields": ["$.name", "$.cost"]
  },
  "body": {
    "query": "query getItems{pokemon_v2_item{name,cost}}",
    "variables": null,
    "operationName": "getItems"
  }
}
```

Additionally, you can provide a `result` object with a JSONPath to the "root" in the response,
and/or a list of `fields`:

```json
{
  "url": "https://pokeapi.co/api/v2/pokemon?limit=100&offset=200",
  "result": {
    "path": "$.results",
    "fields": ["$.name"]
  }
}
```

You may also be able to use query placeholders as well if you haev defined a table
specific definition. E.g.: `{{endpoint}}` in the below definition:

```json
{
  "url": "https://pokeapi.co/api/v2/{{endpoint}}/?limit=10",
  "fields": [
    {
      "name": "$.name",
      "type": "string"
    },
    {
      "name": "$.url",
      "type": "string"
    }
  ],
  "result": {
    "path": "$.results"
  }
}
```

### Table Administration

Within the driver Admin configuration page, you can predefine "tables" in the database
configuration's `Table Definitions` setting.

These tables will appear under the database name within the browse data section:

```json
{
  "tables": [
    {
      "name": "Ability",
      "url": "https://pokeapi.co/api/v2/ability/?limit=20&offset=20",
      "fields": [
        { "name": "$.name", "type": "string" },
        { "name": "$.url", "type": "string" }
      ],
      "result": {
        "path": "$.results"
      }
    }
  ]
}
```

The JSON is similar to examples provided in the "Within Native/SQL queries" section above
except that the definitions are nested within a `{"tables": []}` object list

## Limitations

- There is limited support for aggregations, breakouts, table storing, or joining.
- There are security risks to using credentials directly in table views. These should be defined
  in the admin panel to prevent against secrets being exposed.
- This is an experimental driver and may be removed or unsupported in future versions of Metabase.
- THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
  AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

## Building the driver

> These directions are for plugin developers or individuals who wish to make changes to the plugin

[Docker compose](https://docs.docker.com/get-docker/) will download and install Metabase
and then build the driver. You can specify the specific version of Metabase to build against
in the [Dockerfile](./Dockerfile) `METABASE_VERSION` argument.

Run the following to start a new build:

```shell
docker-compose up
```

This will update the [http.metabase-driver.jar](dist/http.metabase-driver.jar) file in
the `dist` folder and also compile the java source in a `target` folder in the project directory.

## Installing the driver

Copy the [http.metabase-driver.jar](dist/http.metabase-driver.jar) file to your
[metabase plugins folder](https://www.metabase.com/docs/latest/developers-guide/partner-and-community-drivers#how-to-use-a-third-party-driver)
and restart your server, and the driver will show up after that point as a
database connection that can be enabled in the Admin panel.
