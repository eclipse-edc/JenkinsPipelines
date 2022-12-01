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
                    sh './scripts/start_build.sh $OWNER $REPO $WORKFLOW $BOT $BOTTOKEN'
                }
            }
        }
        stage("wait-for-job-created") {
            steps {
                timeout(60) {
                    withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'BOTTOKEN', usernameVariable: 'BOT')]) {
                        sh './scripts/wait_for_build.sh $OWNER $REPO $WORKFLOW $BOT $BOTTOKEN'
                    }
                }
            }
        }
    }
}
