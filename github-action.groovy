pipeline {
    agent none
    stages {
        stage("start-github-workflow") {
            steps {
                withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'BOTTOKEN', usernameVariable: 'BOT')]) {
                    sh './scripts/start_build.sh $OWNER $REPO $WORKFLOW $BOT $BOTTOKEN'
                }
            }
        }
        stage("wait-until-job-created") {
            steps {
                timeout(30) {
                    withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'BOTTOKEN', usernameVariable: 'BOT')]) {
                        // this will create a file "run.id" that contains the run-id
                        sh './scripts/wait_job_created.sh $OWNER $REPO $WORKFLOW $BOT $BOTTOKEN'
                    }
                }
            }
        }
        stage("wait-until-job-completed") {
            steps {
                script {
                    run_id = readFile('run.id')
                    currentBuild.displayName = "$OWNER/$REPO/$WORKFLOW/$run_id"
                }
                timeout(60) {
                    withCredentials([usernamePassword(credentialsId: 'github-bot', passwordVariable: 'BOTTOKEN', usernameVariable: 'BOT')]) {
                        sh './scripts/wait_job_completed.sh $OWNER $REPO $BOT $BOTTOKEN'
                    }
                }
            }
        }
    }
}
