pipeline {
    agent any
    stages {
        stage('runtime-metamodel') {
            steps {
                build job: 'Start-Github-Action', parameters: [string(name: 'OWNER', value: 'eclipse-edc'), string(name: 'REPO', value: 'gradleplugins'), string(name: 'WORKFLOW', value: 'test.yaml')]
                build job: '../Build-Component-Template', parameters: [string(name: 'REPO', value: 'https://github.com/eclipse-edc/GradlePlugins.git')]
            }
        }

        stage('connector') {
            steps {
                build job: 'Start-Github-Action', parameters: [string(name: 'OWNER', value: 'eclipse-edc'), string(name: 'REPO', value: 'connector'), string(name: 'WORKFLOW', value: 'verify.yaml')]
                build job: '../Build-Component-Template', parameters: [string(name: 'REPO', value: 'https://github.com/eclipse-edc/Connector.git')]
            }
        }

        stage("identityhub") {
            steps {
                build job: 'Start-Github-Action', parameters: [string(name: 'OWNER', value: 'eclipse-edc'), string(name: 'REPO', value: 'identityhub'), string(name: 'WORKFLOW', value: 'verify.yaml')]
                build job: '../Build-Component-Template', parameters: [string(name: 'REPO', value: 'https://github.com/eclipse-edc/IdentityHub.git')]
            }
        }

        stage("registration-service") {
            steps {
                build job: 'Start-Github-Action', parameters: [string(name: 'OWNER', value: 'eclipse-edc'), string(name: 'REPO', value: 'registrationservice'), string(name: 'WORKFLOW', value: 'verify.yaml')]
                build job: '../Build-Component-Template', parameters: [string(name: 'REPO', value: 'https://github.com/eclipse-edc/RegistrationService.git')]
            }

        }
        stage("federated-catalog") {
            steps {
                build job: 'Start-Github-Action', parameters: [string(name: 'OWNER', value: 'eclipse-edc'), string(name: 'REPO', value: 'federatedcatalog'), string(name: 'WORKFLOW', value: 'verify.yaml')]
                build job: '../Build-Component-Template', parameters: [string(name: 'REPO', value: 'https://github.com/eclipse-edc/FederatedCatalog.git')]
            }
        }
        stage("test-mvd") {
            parallel {
                stage("run-embedded-resources") {
                    steps {
                        build job: 'Start-Github-Action', parameters: [string(name: 'OWNER', value: 'eclipse-edc'), string(name: 'REPO', value: 'minimumviabledataspace'), string(name: 'WORKFLOW', value: 'cd.yaml')]
                    }
                }
//                stage("run-with-azure-resources") {
//                    steps {
//                        build job: 'Start-Github-Action', parameters: [string(name: 'OWNER', value: 'eclipse-edc'), string(name: 'REPO', value: 'minimumviabledataspace'), string(name: 'WORKFLOW', value: 'cloud-cd.yaml')]
//                    }
//                }
            }

        }

    }
    // post {
    //     always {
    //         build(job: "../../DiscordWebhook", parameters: [string(name: "UPSTREAM_JOB_URL", value: "${env.BUILD_URL}"), string(name: "JOB_NAME", value: "${env.JOB_NAME}"), string(name: "BUILD_NUMBER", value: "${env.BUILD_NUMBER}"), string(name: "REPO_URL", value: "https://github.com/eclipse-edc/Connector"), string(name: "CONTENT", value: "Look, I built a SNAPSHOT version of all components!"), string(name: "WEBHOOK_URL", value: "https://discord.com/api/webhooks/1044851842754027622/vLIhAy_eTODGk5GZbsYJSV351bQVJPvCGpw57AeJxqIVJ0eN11tlH8FCj3HayXjefyzz")])
    //     }
    // }
}
