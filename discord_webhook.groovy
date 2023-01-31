pipeline {
    agent any
    stages {

        stage("invoke-webhook") {
            steps{
                sh 'chmod +x scripts/discord_webhook.sh'
                sh '''
                    status=$(curl --silent ${UPSTREAM_JOB_URL}api/json | jq -r \'.result\' | awk \'{print tolower($0)}\')
                    ./scripts/discord_webhook.sh "${status}" "${JOB_NAME}" "${BUILD_NUMBER}" "${REPO_URL}" "${CONTENT}"
                '''
            }
        }

    }
}
