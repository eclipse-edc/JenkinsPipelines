# Jenkins Pipelines

This repository contains the Jenkins Pipelines used for the EDC CI:
https://ci.eclipse.org/edc/

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
- `VERSION`: the version to be published. If it doesn't end with `-SNAPSHOT`, the artifact will be published on maven central

## [publish_template](./publish_template.groovy)
Builds and publish a single component.

Variables:
- `REPO`: the component repository
- `VERSION`: the version to be published
- `BRANCH`: the branch to be published
