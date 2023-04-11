pipeline {
    agent any
    environment {
        // this could be obtained from somewhere else, e.g. a file on github
        VERSION = """${sh(returnStdout: true, script: 'echo "0.0.1-$(date +"%Y%m%d")-SNAPSHOT"')}""".trim()
    }
    stages {
        stage('init') {
            steps {
                script {
                    currentBuild.displayName = sh(script: 'echo "$VERSION"', returnStdout: true).trim()
                }
            }
        }
        stage('test-runtime-metamodel') {
            steps {
                timeout(60) {
                    withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'BOTTOKEN', usernameVariable: 'BOT')]) {
                        sh './scripts/github_action.sh "eclipse-edc" "gradleplugins" "test.yaml" $BOT $BOTTOKEN'
                    }
                }
            }
        }

        stage('test-connector') {
            steps {
                timeout(60) {
                    withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'BOTTOKEN', usernameVariable: 'BOT')]) {
                        sh './scripts/github_action.sh "eclipse-edc" "connector" "verify.yaml" $BOT $BOTTOKEN'
                    }
                }
            }
        }

        stage("test-identityhub") {
            steps {
                timeout(60) {
                    withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'BOTTOKEN', usernameVariable: 'BOT')]) {
                        sh './scripts/github_action.sh "eclipse-edc" "identityhub" "verify.yaml" $BOT $BOTTOKEN'
                    }
                }
            }
        }

        stage("test-registration-service") {
            steps {
                timeout(60) {
                    withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'BOTTOKEN', usernameVariable: 'BOT')]) {
                        sh './scripts/github_action.sh "eclipse-edc" "registrationservice" "verify.yaml" $BOT $BOTTOKEN'
                    }
                }
            }

        }
        stage("test-federated-catalog") {
            steps {
                timeout(60) {
                    withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'BOTTOKEN', usernameVariable: 'BOT')]) {
                        sh './scripts/github_action.sh "eclipse-edc" "federatedcatalog" "verify.yaml" $BOT $BOTTOKEN'
                    }
                }
            }
        }
        stage("test-mvd") {
            steps {
                timeout(60) {
                    withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'BOTTOKEN', usernameVariable: 'BOT')]) {
                        sh './scripts/github_action.sh "eclipse-edc" "minimumviabledataspace" "cd.yaml" $BOT $BOTTOKEN'
                    }
                }
            }
        }
        stage('build-publish-components') {
            steps {
                build job: 'Publish-All-In-One', parameters: [string(name: 'VERSION', value: "${VERSION}")]
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
