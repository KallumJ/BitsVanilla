pipeline {
    agent any

    stages {
        stage('build') {
            steps {
                ./gradlew clean build
                archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
            }
        }
    }
}