pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        sh '''
./gradlew clean assemble
'''
    stash name: 'fatJar', includes: 'build/libs/*fat.jar'
      }
    }
    stage('Deploy') {
      steps {
        unstash 'fatJar'
        withCredentials(bindings: [sshUserPrivateKey(credentialsId: 'jenkins_start.vertx.io', \
                                                             keyFileVariable: 'SSH_KEY_FOR_START_VERTX_IO')]) {
          sh '''
set +x

if [ -z ${REMOTE_USER+x} ] || [ -z ${REMOTE_HOSTNAME+x} ]; then
  echo "Fatal: SSH information not set."
  exit 1
fi

echo "Sending jar with ${REMOTE_USER}@${REMOTE_HOSTNAME}"
ssh -i "${SSH_KEY_FOR_START_VERTX_IO}" "${REMOTE_USER}"@"${REMOTE_HOSTNAME}" mkdir -p "${TEMP}"
scp -i "${SSH_KEY_FOR_START_VERTX_IO}" ./build/libs/*fat.jar "${REMOTE_USER}"@"${REMOTE_HOSTNAME}":"${TEMP}"
'''

          sh '''
set +x

if [ -z ${REMOTE_USER+x} ] || [ -z ${REMOTE_HOSTNAME+x} ]; then
  echo "Fatal: SSH information not set."
  exit 1
fi

echo "Deploying with ${REMOTE_USER}@${REMOTE_HOSTNAME}"

ssh -i "${SSH_KEY_FOR_START_VERTX_IO}" "${REMOTE_USER}"@"${REMOTE_HOSTNAME}" "sudo -t bash -s" < ./scripts/deploy.sh
'''
        }
      }
    }
  }
  environment {
    REMOTE_USER = 'jenkins'
    REMOTE_HOSTNAME = 'start.vertx.io'
    TEMP= '/tmp/vertx'
  }
}
