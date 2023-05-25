pipeline {
    agent any
    environment {
        // this will read the current version gradle.properties (taken from the connector's main branch's) into variables, and then use them to construct
        // the X.Y.Z-<DATE>-SNAPSHOT version
        VERSION = """${sh(returnStdout: true, script:
                'wget -cq https://raw.githubusercontent.com/eclipse-edc/Connector/main/gradle.properties && IFS=.- read -r RELEASE_VERSION_MAJOR RELEASE_VERSION_MINOR RELEASE_VERSION_PATCH SNAPSHOT<<<$(grep "version" gradle.properties | awk -F= \'{print $2}\') && echo "$RELEASE_VERSION_MAJOR.$RELEASE_VERSION_MINOR.$RELEASE_VERSION_PATCH-$(date +"%Y%m%d")-SNAPSHOT"'
        )}""".trim()
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
