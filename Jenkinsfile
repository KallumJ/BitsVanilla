pipeline {
    agent any

    tools {
        jdk 'jdk_16'
    }

    stages {
        stage('build') {
            steps {
                sh './gradlew clean build'
                archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
            }
        }
    }
}