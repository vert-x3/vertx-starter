set -e

echo "Extracting Starter version..."
STARTER_VERSION=$(./gradlew properties -q | grep "^version:" | awk '{print $2}')
echo "Starter version is ${STARTER_VERSION}"

STARTER_JAR="./build/libs/vertx-starter-${STARTER_VERSION}-fat.jar"

if [ ! -f ${STARTER_JAR} ]; then
  echo "Could not find file ${STARTER_JAR}"
  exit 1
fi

REMOTE_USER="jenkins"
REMOTE_HOSTNAME="start.vertx.io"
REMOTE_TEMP="/tmp/vertx"

echo "Sending JAR to ${REMOTE_USER}@${REMOTE_HOSTNAME}..."
ssh "${REMOTE_USER}"@"${REMOTE_HOSTNAME}" rm -rf "${REMOTE_TEMP}"
scp "${STARTER_JAR}" "${REMOTE_USER}"@"${REMOTE_HOSTNAME}":"${REMOTE_TEMP}"
echo "JAR sent"

echo "Deploying with ${REMOTE_USER}@${REMOTE_HOSTNAME}..."
ssh "${REMOTE_USER}"@"${REMOTE_HOSTNAME}" "sudo -t bash -s" < ./scripts/deploy.sh
echo "Starter deployed!"
