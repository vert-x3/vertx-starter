set -e

echo "Extracting Starter version..."
STARTER_VERSION=$(./gradlew properties -q | grep "^version:" | awk '{print $2}')
echo "Starter version is ${STARTER_VERSION}"

STARTER_JAR_NAME="vertx-starter-${STARTER_VERSION}-fat.jar"
STARTER_JAR="./build/libs/${STARTER_JAR_NAME}"

if [ ! -f ${STARTER_JAR} ]; then
  echo "Could not find file ${STARTER_JAR}"
  exit 1
fi

REMOTE_USER="jenkins"
REMOTE_HOSTNAME="start.vertx.io"
REMOTE_TEMP="/tmp/vertx"

echo "Sending JAR to ${REMOTE_USER}@${REMOTE_HOSTNAME}..."
ssh "${REMOTE_USER}"@"${REMOTE_HOSTNAME}" rm -rf "${REMOTE_TEMP}"
ssh "${REMOTE_USER}"@"${REMOTE_HOSTNAME}" mkdir -p "${REMOTE_TEMP}"
scp "${STARTER_JAR}" "${REMOTE_USER}"@"${REMOTE_HOSTNAME}":"${REMOTE_TEMP}/${STARTER_JAR_NAME}"
echo "JAR sent"

echo "Deploying with ${REMOTE_USER}@${REMOTE_HOSTNAME}..."
ssh "${REMOTE_USER}"@"${REMOTE_HOSTNAME}" "sudo -t bash -s" < ./scripts/deploy.sh
echo "Starter deployed!"
