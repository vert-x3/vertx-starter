#!/usr/bin/env bash

# The script consider the fat jar is already present on the VM, and the service relies on systemd

TEMP=${1:-/tmp/vertx}
VERTX_HOME=${VERTX_HOME:-/opt/vertx}

if [ ! -d "${VERTX_HOME}" ]; then
  mkdir -p "${VERTX_HOME}"
fi

if [ ! -d "${TEMP}" ]; then
  echo "Fatal: ${TEMP} does not exists."
  exit 1
fi

cd "${TEMP}" || exit
JAR=$(ls *fat.jar)

if [ ! -f "${JAR}" ]; then
  echo "Fatal: No jar found in ${TEMP}."
  exit 1
fi

echo "Found jar: ${JAR}"

mv "${JAR}" "${VERTX_HOME}"
rm -rf "${TEMP}"
sudo chown vertx:vertx "${VERTX_HOME}/${JAR}"
sudo chmod 700 "${VERTX_HOME}/${JAR}"

readonly vertx_starter_latest_jar="${VERTX_HOME}/vertx-starter-latest-fat.jar"

rm "${vertx_starter_latest_jar}"
sudo -u vertx ln -s "${VERTX_HOME}/${JAR}" "${vertx_starter_latest_jar}"
sudo systemctl restart vertx-starter
