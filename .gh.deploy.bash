set -e

echo "Extracting Starter version..."
STARTER_VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=project.version -B | grep -v '\[')
echo "Starter version is ${STARTER_VERSION}"

STARTER_JAR_NAME="vertx-starter-${STARTER_VERSION}-fat.jar"
STARTER_JAR="./target/${STARTER_JAR_NAME}"

if [ ! -f ${STARTER_JAR} ]; then
  echo "Could not find file ${STARTER_JAR}"
  exit 1
fi

REMOTE_USER="vertx-deploy"
REMOTE_HOSTNAME="13.94.149.21"

echo "Sending JAR to ${REMOTE_USER}@${REMOTE_HOSTNAME}..."
scp "${STARTER_JAR}" "${REMOTE_USER}"@"${REMOTE_HOSTNAME}":latest-build
echo "JAR sent"
