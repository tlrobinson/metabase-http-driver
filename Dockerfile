FROM node:16-slim as metabase_driver

# Metabase Version: https://github.com/metabase/metabase/releases
ARG METABASE_VERSION=v0.45.3

# Install environment dependencies
RUN apt-get update  \
    && apt-get upgrade -y  \
    && apt-get install openjdk-11-jdk curl git -y \
    && curl -O https://download.clojure.org/install/linux-install-1.11.1.1208.sh \
    && chmod +x linux-install-1.11.1.1208.sh \
    && ./linux-install-1.11.1.1208.sh \

# Download Metabase and install dependencies
# We ignore errors here since a successful Metabase build is not always required to build the driver
WORKDIR /
RUN git clone --branch $METABASE_VERSION --depth 1 https://github.com/metabase/metabase /metabase
RUN cp -r /metabase /home/node/
RUN cd /home/node && clojure -X:dev:deps | > /dev/null

# Copy the local build files and build the driver
WORKDIR /build/
COPY . .

ENTRYPOINT clojure -X:dev:build