FROM node:16-slim as metabase_driver

# Metabase Version: https://github.com/metabase/metabase/releases
ARG METABASE_VERSION=v0.43.3

# Install environment dependencies
RUN apt-get update  \
    && apt-get upgrade -y  \
    && apt-get install openjdk-11-jdk curl git -y \
    && curl -O https://download.clojure.org/install/linux-install-1.11.1.1237.sh \
    && chmod +x linux-install-1.11.1.1237.sh \
    && ./linux-install-1.11.1.1237.sh \

# Download Metabase and install dependencies
WORKDIR /
RUN git clone --branch $METABASE_VERSION --depth 1 https://github.com/metabase/metabase /metabase
RUN cd /metabase/ && clojure -M:dev:deps

# Copy the local build files and build the driver
WORKDIR /build/
COPY . /build

ENTRYPOINT clojure -X:build