pipeline {
    agent any

    tools {
        jdk 'jdk_16'
    }

    stages {
        stage('build') {
            steps {
		sh "sed -i 's/%VERSION%/${BRANCH_NAME}-${BUILD_NUMBER}/' gradle.properties"
		sh 'cat gradle.properties'
                sh './gradlew clean build'
                archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
            }
        }
    }
}
