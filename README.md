# Jenkins Pipelines

This repository contains the Jenkins Pipelines used for the EDC CI:
https://ci.eclipse.org/edc/

## [build_template](./build_template.groovy)
Builds and publish a single component.

Variables:
- `REPO`: the component repository
- `VERSION`: the version to be built
- `BRANCH`: the branch to be built

## [discord_webhook](./discord_webhook.groovy)
Sends a discord webhook advising that a job has been completed.

Variables:
- `UPSTREAM_JOB_URL`: the Jenkins Job URL 
- `JOB_NAME`: the Jenkins Job name
- `BUILD_NUMBER`: the Jenkins Job build number
- `REPO_URL`: the component url
- `CONTENT`: text that will be added to the message

## [github-action](./github-action.groovy)
Starts a GitHub action workflow and waits for its completion.

Variables:
- `OWNER`: the organization name 
- `REPO`: the repository name 
- `WORKFLOW`: the workflow to be launched

## [nightly.groovy](./nightly.groovy)
Builds and publish all the components every night.

## [publish-all-in-one](./publish-all-in-one.groovy)
Builds and publish all the components.

Variables:
- `VERSION`: the version to be built. If it doesn't end with `-SNAPSHOT`, the artifact will be published on maven central
