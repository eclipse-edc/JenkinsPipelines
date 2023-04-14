pipeline {
    agent any
    environment {

    }
    stages {
        stage('init') {
            steps {
                script {
                    currentBuild.displayName = sh(script: 'echo "$VERSION"', returnStdout: true).trim()
                }
            }
        }

        stage('test') {
            steps {
                timeout(120) {
                    withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'BOTTOKEN', usernameVariable: 'BOT')]) {
                        sh './scripts/test_components.sh $BOT $BOTTOKEN'
                    }
                }
            }
        }

        stage('publish-artifacts') {
            steps {
                  build job: 'Publish-All-In-One', parameters: [string(name: 'VERSION', value: "${VERSION}")]
            }
        }

        stage('release') {
            steps {
                timeout(60) {
                    withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'BOTTOKEN', usernameVariable: 'BOT')]) {
                        sh './scripts/release_components.sh $VERSION $BOT $BOTTOKEN'
                    }
                }
            }
        }

    }

    post {
        always {
            withCredentials([string(credentialsId: 'discord-webhook', variable: 'WEBHOOK_URL')]) {
                cleanWs()
                checkout scm
                sh """
                    chmod +x scripts/discord_webhook.sh
                    ./scripts/discord_webhook.sh "${currentBuild.getCurrentResult()}" "${env.JOB_NAME}" "${env.BUILD_NUMBER}" "https://github.com/eclipse-edc/Connector" "${VERSION}"
                """
            }
        }
    }
}
