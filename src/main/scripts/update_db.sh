#!/bin/bash

set -e

if [ $# -ne 1 ]; then
  echo "Expected exactly one argument"
  exit 1
fi

ANALYTICS_DIR=$1

if [ ! -d "${ANALYTICS_DIR}" ]; then
  echo "${ANALYTICS_DIR} is not a directory"
  exit 1
fi

if [ -z "$(ls -A "$ANALYTICS_DIR")" ]; then
  echo "No files in analytics directory."
  exit 0
fi

DATABASE_FILE=vertx-starter.db

if [ ! -f "${DATABASE_FILE}" ]; then
  sqlite3 "${DATABASE_FILE}" <<EOF
create table projects
(
  language           TEXT,
  build_tool         TEXT,
  vertx_version      TEXT,
  vertx_dependencies TEXT,
  archive_format     TEXT,
  jdk_version        TEXT,
  operating_system   TEXT,
  created_on         TEXT
);
EOF
fi

WORK_DIR=work-dir
mkdir "${WORK_DIR}"

mv "$ANALYTICS_DIR"/* "$WORK_DIR"

for file in "$WORK_DIR"/*; do
  json=$(cat "$file")
  sqlite3 "${DATABASE_FILE}" "INSERT INTO projects
  (
  language,
  build_tool,
  vertx_version,
  vertx_dependencies,
  archive_format,
  jdk_version,
  operating_system,
  created_on
  )
  VALUES
  (
  json_extract('${json}', '$.language'),
  json_extract('${json}', '$.buildTool'),
  json_extract('${json}', '$.vertxVersion'),
  json_extract('${json}', '$.vertxDependencies'),
  json_extract('${json}', '$.archiveFormat'),
  json_extract('${json}', '$.jdkVersion'),
  json_extract('${json}', '$.operatingSystem'),
  json_extract('${json}', '$.createdOn')
  )
  "
done

rm -rf ${WORK_DIR}
