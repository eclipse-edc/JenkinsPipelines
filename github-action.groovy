pipeline {
    agent any
    environment {
        WORKFLOW= 'test.yaml'
    }
    stages {
        stage("setup-credentials") {
            steps {
                withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'BOTTOKEN', usernameVariable: 'BOT')]) {
                    sh (script:
                            '''curl --fail -X POST -u $BOT:$BOTTOKEN -H \'Accept: application/vnd.github.v3+json\' -d \'{"ref":"main"}\' "https://api.github.com/repos/eclipse-edc/GradlePlugins/actions/workflows/test.yaml/dispatches"''',
                            returnStdout: true)
                }
            }
        }
    }
}
