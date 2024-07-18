pipeline {
    agent { label 'docker' }
    options {
        buildDiscarder(logRotator(numToKeepStr: '50'))
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
    }
    environment {
        DOCKER_IMAGE = 'siglusdevops/android-runner'
        CONTAINER_NAME = 'siglus-android-runner'
    }
    stages {
        stage('Setup Docker Container') {
            steps {
                script {
                    sh """
                       docker run -d --name ${CONTAINER_NAME} --network host --security-opt seccomp=unconfined \
                       -v ${pwd()}:/app -w /app ${DOCKER_IMAGE} tail -f /dev/null
                    """
                }
            }
        }
        stage('Static Code Analysis') {
            steps {
                script {
                    executeInContainer('./gradlew clean checkstyle pmd spotbugsLocalDebug')
                }
            }
        }
        stage('Unit Test') {
            steps {
                script {
                    executeInContainer('./gradlew testLocalDebugUnitTest --debug --daemon --build-cache')
                }
            }
        }
        stage('Test Coverage Verification') {
            steps {
                script {
                    executeInContainer('./gradlew jacocoTestCoverageVerification')
                }
            }
        }
    }
    post {
        always {
            script {
                sh "docker rm -f ${CONTAINER_NAME}"
            }
        }
    }
}

def executeInContainer(cmd) {
    sh """
       docker exec ${CONTAINER_NAME} sh -c 'JAVA_OPTS="-Xmx8192m -XX:MaxPermSize=4096m" ${cmd}'
    """
}
