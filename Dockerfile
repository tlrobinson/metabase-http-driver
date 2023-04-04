FROM node:16-slim as metabase_driver

# Metabase Version: https://github.com/metabase/metabase/releases
# Clojure Version: https://clojure.org/guides/install_clojure
ARG METABASE_VERSION=v0.45.3
ARG CLOJURE_VERSION=1.11.1.1252

# Install environment dependencies
RUN apt-get update && apt-get upgrade --yes
RUN apt-get install openjdk-11-jdk curl git --yes
RUN curl -O https://download.clojure.org/install/linux-install-$CLOJURE_VERSION.sh
RUN chmod +x linux-install-$CLOJURE_VERSION.sh
RUN ./linux-install-$CLOJURE_VERSION.sh

# Download Metabase and install dependencies
WORKDIR /
RUN git clone --branch $METABASE_VERSION --depth 1 https://github.com/metabase/metabase /metabase
RUN cd /metabase/ && clojure -M:dev:deps

# Copy the local build files and build the driver
WORKDIR /build/
COPY . /build

ENTRYPOINT clojure --version && clojure -X:build