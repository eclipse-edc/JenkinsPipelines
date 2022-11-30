pipeline {
    agent any
    environment {
        REPO = 'https://github.com/paullatzelsperger/GradlePlugins'
        WORKFLOW= 'test.yaml'
    }
    stages {
        stage("setup-credentials") {
            steps {
//                withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'BOTTOKEN', usernameVariable: 'BOT')]) {
                    sh (script: '''curl -sSL -X POST \\
                        -H "Authorization: Bearer ${TOKEN}"\\
                        -H "Accept: application/vnd.github.v3+json" \\
                        -d '{"ref":"main"}' \\
                        https://api.github.com/repos/paullatzelsperger/GradlePlugins/actions/workflows/test.yaml/dispatches''', returnStdout: true)
//                }
            }
        }
    }
}
