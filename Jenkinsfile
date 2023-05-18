pipeline {
    agent { label 'docker'}
    options {
        buildDiscarder(logRotator(numToKeepStr: '50'))
        timestamps ()
    }
    stages {
        stage('Static Code Analysis') {
            steps {
                executeInContainer('./gradlew clean checkstyle pmd spotbugsLocalDebug')
            }
        }
        stage('Unit Test') {
            steps {
                executeInContainer('./gradlew testLocalDebugUnitTest --debug')
            }
        }
        stage('Test Coverage Verification') {
            steps {
                executeInContainer('./gradlew jacocoTestCoverageVerification')
            }
        }
        stage('Sonarqube Analysis') {
            when {
                environment name: 'GIT_BRANCH', value: 'master'
            }
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONARQUBE_TOKEN')]) {
                    executeInContainer("./gradlew sonarqube -x test -Dsonar.projectKey=siglus-android -Dsonar.host.url=http://localhost:9000 -Dsonar.login=${SONARQUBE_TOKEN}")
                }
            }
        }
    }
}

def executeInContainer(cmd) {
    withEnv(["CMD=${cmd}"]) {
        withCredentials([string(credentialsId: 'KSTOREPWD', variable: 'KSTOREPWD'),string(credentialsId: 'KEYPWD', variable: 'KEYPWD')]) {
            sh '''
               docker run --rm -v `pwd`:/app -w /app --network=host \
               --security-opt seccomp=unconfined \
               -e KSTOREPWD \
               -e KEYPWD \
               siglusdevops/android-runner sh -c \
               "${CMD}"
            '''
        }
    }
}
