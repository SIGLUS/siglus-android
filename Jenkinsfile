pipeline {
    agent any
    options {
        buildDiscarder(logRotator(numToKeepStr: '50'))
        timestamps ()
    }
    stages {
        stage('Build') {
            steps {
                println "gradle: build"
                sh '''
                    pwd && ls -l
                    ./gradlew clean checkstyle pmd testLocalDebug
                '''
                println "sonarqube: analysis"
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONARQUBE_TOKEN')]) {
                    sh '''
                        if [ "$GIT_BRANCH" = "master" ]; then
                            ./gradlew jacoco sonarqube -x test -Dsonar.projectKey=siglus-android -Dsonar.host.url=http://localhost:9000 -Dsonar.login=$SONARQUBE_TOKEN
                        fi
                    '''
                }
            }
        }
    }
}
