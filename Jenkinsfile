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
                    withCredentials([string(credentialsId: 'KSTOREPWD', variable: 'KSTOREPWD'),string(credentialsId: 'KEYPWD', variable: 'KEYPWD')]) {
                        sh """
                           docker run -d --name ${CONTAINER_NAME} --network host --security-opt seccomp=unconfined \
                           -e KSTOREPWD -e KEYPWD \
                           -v ${pwd()}:/app -w /app ${DOCKER_IMAGE} tail -f /dev/null
                        """
                    }
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
        stage('Sonarqube Analysis') {
            when {
                branch 'master'
            }
            steps {
                script {
                    withCredentials([string(credentialsId: 'sonar-token', variable: 'SONARQUBE_TOKEN')]) {
                        executeInContainer("./gradlew sonarqube -x test -Dsonar.projectKey=siglus-android -Dsonar.host.url=http://localhost:9000")
                    }
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
       docker exec ${CONTAINER_NAME} sh -c 'JAVA_OPTS="-Xmx8192m -XX:MaxPermSize=2048m" ${cmd}'
    """
}
