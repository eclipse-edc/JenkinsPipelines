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
        stage('publish-artifacts') {
            steps {
                  build job: 'Publish-All-In-One', parameters: [string(name: 'VERSION', value: "${VERSION}")]
            }
        }

        stage('test-connector') {
            steps {
                timeout(60) {
                    withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'BOTTOKEN', usernameVariable: 'BOT')]) {
                        sh './scripts/github_action.sh "eclipse-edc" "connector" "verify.yaml" "" $BOT $BOTTOKEN'
                    }
                }
            }
        }

        stage("test-identityhub") {
            steps {
                timeout(60) {
                    withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'BOTTOKEN', usernameVariable: 'BOT')]) {
                        sh './scripts/github_action.sh "eclipse-edc" "identityhub" "verify.yaml" "" $BOT $BOTTOKEN'
                    }
                }
            }
        }

        stage("test-registration-service") {
            steps {
                timeout(60) {
                    withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'BOTTOKEN', usernameVariable: 'BOT')]) {
                        sh './scripts/github_action.sh "eclipse-edc" "registrationservice" "verify.yaml" "" $BOT $BOTTOKEN'
                    }
                }
            }

        }
        stage("test-federated-catalog") {
            steps {
                timeout(60) {
                    withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'BOTTOKEN', usernameVariable: 'BOT')]) {
                        sh './scripts/github_action.sh "eclipse-edc" "federatedcatalog" "verify.yaml" "" $BOT $BOTTOKEN'
                    }
                }
            }
        }
        stage("test-mvd") {
            steps {
                timeout(60) {
                    withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'BOTTOKEN', usernameVariable: 'BOT')]) {
                        sh './scripts/github_action.sh "eclipse-edc" "minimumviabledataspace" "cd.yaml" "" $BOT $BOTTOKEN'
                    }
                }
            }
        }

        stage('build-publish-components') {
            steps {
                build job: 'Publish-All-In-One', parameters: [string(name: 'VERSION', value: "${VERSION}")]
            }
        }

        stage('release-gradleplugins') {
            steps {
                timeout(60) {
                    withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'BOTTOKEN', usernameVariable: 'BOT')]) {
                        sh './scripts/github_action.sh "eclipse-edc" "gradleplugins" "release-all-java.yaml" "{\"metamodel_version\", \"${VERSION}\"}" $BOT $BOTTOKEN'
                    }
                }
            }
        }

        stage('release-connector') {
            steps {
                timeout(60) {
                    withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'BOTTOKEN', usernameVariable: 'BOT')]) {
                        sh './scripts/github_action.sh "eclipse-edc" "connector" "release-edc.yaml" "{\"edc_version\", \"${VERSION}\"}" $BOT $BOTTOKEN'
                    }
                }
            }
        }

        stage('release-identityhub') {
            steps {
                timeout(60) {
                    withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'BOTTOKEN', usernameVariable: 'BOT')]) {
                        sh './scripts/github_action.sh "eclipse-edc" "identityhub" "release-identityhub.yaml" "{\"ih_version\", \"${VERSION}\"}" $BOT $BOTTOKEN'
                    }
                }
            }
        }

        stage('release-registrationservice') {
            steps {
                timeout(60) {
                    withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'BOTTOKEN', usernameVariable: 'BOT')]) {
                        sh './scripts/github_action.sh "eclipse-edc" "registrationservice" "release-registrationservice.yaml" "{\"rs_version\", \"${VERSION}\"}" $BOT $BOTTOKEN'
                    }
                }
            }
        }

        stage('release-federatedcatalog') {
            steps {
                timeout(60) {
                    withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'BOTTOKEN', usernameVariable: 'BOT')]) {
                        sh './scripts/github_action.sh "eclipse-edc" "federatedcatalog" "release-fcc.yaml" "{\"edc_version\", \"${VERSION}\"}" $BOT $BOTTOKEN'
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
