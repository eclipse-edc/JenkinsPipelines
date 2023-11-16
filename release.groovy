pipeline {
    agent any
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

        // during the 'release' stage, every repo gets its version bumped, so we need to publish again, sequentially
        // at this time, all components should already have their snapshot version bumped, so no need to explicitly supply the VERSION parameter
        stage('publish-new-snapshot') {
            steps {
                build job: 'Publish-Component', parameters: [string(name: 'REPO', value: "https://github.com/eclipse-edc/Runtime-Metamodel")]
                build job: 'Publish-Component', parameters: [string(name: 'REPO', value: "https://github.com/eclipse-edc/GradlePlugins")]
                build job: 'Publish-Component', parameters: [string(name: 'REPO', value: "https://github.com/eclipse-edc/Connector")]
                build job: 'Publish-Component', parameters: [string(name: 'REPO', value: "https://github.com/eclipse-edc/IdentityHub")]
                build job: 'Publish-Component', parameters: [string(name: 'REPO', value: "https://github.com/eclipse-edc/RegistrationService")]
                build job: 'Publish-Component', parameters: [string(name: 'REPO', value: "https://github.com/eclipse-edc/FederatedCatalog")]
                build job: 'Publish-Component', parameters: [string(name: 'REPO', value: "https://github.com/eclipse-edc/Technology-Aws")]
                build job: 'Publish-Component', parameters: [string(name: 'REPO', value: "https://github.com/eclipse-edc/Technology-Azure")]
                build job: 'Publish-Component', parameters: [string(name: 'REPO', value: "https://github.com/eclipse-edc/Technology-Gcp")]
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
