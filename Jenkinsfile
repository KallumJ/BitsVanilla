pipeline {
    agent any

    tools {
        jdk 'jdk_16'
    }

    stages {
        stage('build') {
            steps {
                sh "sed -i 's/%VERSION%/${BRANCH_NAME}-${BUILD_NUMBER}/' gradle.properties"
                sh './gradlew clean build'
                archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
            }
        }
    }

    post {
        always {
            discordSend link: env.BUILD_URL, result: currentBuild.currentResult, title: JOB_NAME, webhookURL: "https://discord.com/api/webhooks/861942609063706634/q7Hk_M2XtH2negfiYGws9EZuQEpUEw8FbCKhvy3PXl59a8qg_knBxsGfr8bP3LZORSkb"
        }
    }
}
