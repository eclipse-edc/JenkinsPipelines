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
                build job: 'Start-Github-Action', parameters: [string(name: 'OWNER', value: 'eclipse-edc'), string(name: 'REPO', value: 'gradleplugins'), string(name: 'WORKFLOW', value: 'test.yaml')]
            }
        }

        stage('test-connector') {
            steps {
                build job: 'Start-Github-Action', parameters: [string(name: 'OWNER', value: 'eclipse-edc'), string(name: 'REPO', value: 'connector'), string(name: 'WORKFLOW', value: 'verify.yaml')]
            }
        }

        stage("test-identityhub") {
            steps {
                build job: 'Start-Github-Action', parameters: [string(name: 'OWNER', value: 'eclipse-edc'), string(name: 'REPO', value: 'identityhub'), string(name: 'WORKFLOW', value: 'verify.yaml')]
            }
        }

        stage("test-registration-service") {
            steps {
                build job: 'Start-Github-Action', parameters: [string(name: 'OWNER', value: 'eclipse-edc'), string(name: 'REPO', value: 'registrationservice'), string(name: 'WORKFLOW', value: 'verify.yaml')]
            }

        }
        stage("test-federated-catalog") {
            steps {
                build job: 'Start-Github-Action', parameters: [string(name: 'OWNER', value: 'eclipse-edc'), string(name: 'REPO', value: 'federatedcatalog'), string(name: 'WORKFLOW', value: 'verify.yaml')]
            }
        }
        stage("test-mvd") {
            steps {
                build job: 'Start-Github-Action', parameters: [string(name: 'OWNER', value: 'eclipse-edc'), string(name: 'REPO', value: 'minimumviabledataspace'), string(name: 'WORKFLOW', value: 'cd.yaml')]
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
            cleanWs()
            checkout scm
            sh """
                chmod +x scripts/discord_webhook.sh
                ./scripts/discord_webhook.sh "${currentBuild.getCurrentResult()}" "${env.JOB_NAME}" "${env.BUILD_NUMBER}" "https://github.com/eclipse-edc/Connector" "Look, I built a nightly version ${VERSION} of the components!"
            """
        }
    }
}
