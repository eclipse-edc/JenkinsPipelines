pipeline {
    agent any
    environment {
        OWNER = 'eclipse-edc'
        REPO = 'GradlePlugins'
        WORKFLOW = 'test.yaml'
    }
    stages {
        stage("start-github-workflow") {
            steps {
                withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'BOTTOKEN', usernameVariable: 'BOT')]) {
                    sh(script: '''
                            curl --fail -X POST -u $BOT:$BOTTOKEN -H \'Accept: application/vnd.github.v3+json\' -d \'{"ref":"main"}\' "https://api.github.com/repos/$OWNER/$REPO/actions/workflows/$WORKFLOW/dispatches"''',
                            returnStdout: true)
                }
            }
        }
        stage("wait-for-job-created") {
            steps {
                waitUntil {

                }
            }
        }
    }
}
