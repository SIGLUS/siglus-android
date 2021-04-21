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
                    ./gradlew clean build
                '''
                println "sonarqube: analysis"
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONARQUBE_TOKEN')]) {
                    sh '''
                        if [ "$GIT_BRANCH" = "master" ]; then
                            echo "SKIP"
                        fi
                    '''
                }
            }
        }
    }
}
