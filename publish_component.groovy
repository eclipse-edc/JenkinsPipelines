pipeline {
    agent any

    stages {
        stage("init") {
            steps {
                script {
                    currentBuild.displayName = sh(script: 'echo "$(cut -d\'/\' -f5 <<< ${REPO}) ${VERSION}"', returnStdout: true).trim()
                }
            }
        }
        stage("clone-repo") {
            steps {
                cleanWs()
                git(branch: "${params.BRANCH}", url: "${params.REPO}")
            }
        }

        stage("setup-credentials") {
            steps {
                withCredentials([file(credentialsId: 'secret-subkeys.asc', variable: 'KEYRING'), string(credentialsId: 'gpg-passphrase', variable: 'PASSPHRASE'), usernamePassword(credentialsId: 'ossrh-account', passwordVariable: 'OSSRH_PASSWORD', usernameVariable: 'OSSRH_USER')]) {
                    sh '''
                    echo $KEYRING
                    gpg --batch --import "${KEYRING}"
                    for fpr in $(gpg --list-keys --with-colons  | awk -F: '/fpr:/ {print $10}' | sort -u);
                    do
                        echo -e "5\\ny\\n" |  gpg --batch --command-fd 0 --expert --edit-key $fpr trust;
                    done
                    '''

                    sh '''
                    # we don't have a TTY, so we need to use the loopback mode
                    echo "use-agent" >> ~/.gnupg/gpg.conf
                    echo "pinentry-mode loopback" >> ~/.gnupg/gpg.conf
                    echo "no-tty" >> ~/.gnupg/gpg.conf
                    '''
                }
            }
        }

        stage("build-component") {
            steps {
                withCredentials([string(credentialsId: 'gpg-passphrase', variable: 'PASSPHRASE'), usernamePassword(credentialsId: 'ossrh-account', passwordVariable: 'OSSRH_PASSWORD', usernameVariable: 'OSSRH_USER')]) {
                    sh '''
                    echo "Will publish ${REPO} with version ${VERSION}"
                    cmd=""
                    versionProp=""

                    if [ ! -z "$VERSION" ]
                    then
                        # set the version property if a version was specified, use defaults otherwise
                        versionProp="-Pversion=$VERSION"

                        # update the version in the codebase
                        sed -i "s#0.0.1-SNAPSHOT#$VERSION#g" gradle.properties
                        sed -i "s#0.0.1-SNAPSHOT#$VERSION#g" $(find . -name "*.java")

                        # if the version doesn't end with -SNAPSHOT we need to handle staging/releasing
                        if [[ $VERSION != *-SNAPSHOT ]]
                        then
                            cmd="closeAndReleaseSonatypeStagingRepository";
                        fi
                    fi

                    ./gradlew publishToSonatype ${cmd} -Psigning.gnupg.keyName=1B4555CF -Psigning.gnupg.passphrase=${PASSPHRASE} -Psigning.gnupg.executable=gpg --no-parallel ${versionProp}
                    '''
                }
            }
        }
    }

    post {
        always {
            build(job: "DiscordWebhook", parameters: [string(name: "UPSTREAM_JOB_URL", value: "${env.BUILD_URL}"), string(name: "JOB_NAME", value: "${env.JOB_NAME}"), string(name: "BUILD_NUMBER", value: "${env.BUILD_NUMBER}"), string(name: "REPO_URL", value: "${params.REPO}"), string(name: "CONTENT", value: "Look, I built a ${VERSION} of ${REPO}")])
        }
    }

}
