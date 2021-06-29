pipeline {
    agent any
    options {
        buildDiscarder(logRotator(numToKeepStr: '50'))
        timestamps ()
    }
    stages {
        stage('Static Code Analysis') {
            steps {
                sh '''
                    pwd && ls -l
                    ./gradlew clean checkstyle pmd spotbugsLocalDebug
                '''
            }
        }
        stage('Unit Test') {
            steps {
                sh '''
                    ./gradlew testLocalDebug --info
                '''
            }
        }
        stage('Test Coverage Verification') {
            steps {
                sh '''
                    ./gradlew jacocoTestCoverageVerification
                '''
            }
        }
        stage('Sonarqube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONARQUBE_TOKEN')]) {
                    sh '''
                        if [ "$GIT_BRANCH" = "master" ]; then
                            ./gradlew sonarqube -x test -Dsonar.projectKey=siglus-android -Dsonar.host.url=http://localhost:9000 -Dsonar.login=$SONARQUBE_TOKEN
                        fi
                    '''
                }
            }
        }
    }
}
