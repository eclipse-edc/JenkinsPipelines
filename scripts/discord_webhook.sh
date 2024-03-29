#!/bin/bash
#   Copyright (c) 2022 Microsoft Corporation
#
#   This program and the accompanying materials are made available under the
#   terms of the Apache License, Version 2.0 which is available at
#   https://www.apache.org/licenses/LICENSE-2.0
#
#   SPDX-License-Identifier: Apache-2.0
#
#   Contributors:
#        Microsoft Corporation - initial implementation

# This file is intended to be used as Post-Build action on CI pipelines to post a message to the EDC's #jenkins-ci Discord
# channel.
# It is loosely based on https://github.com/symboxtra/universal-ci-discord-webhook/blob/master/send.sh, but with many simplifications.

BRANCH_NAME="main"

STATUS="$1"
JENKINS_JOB="$2"
BUILD_NUMBER="$3"
REPO_URL="$4"

if [ -z "$5" ];
then
  VERSION="0.1.0-SNAPSHOT"
else
  VERSION="$5"
fi

CONTENT="${JENKINS_JOB} build ${STATUS}. Version ${VERSION}"

# do not run script if required parameters are not supplied
# the script expect a WEBHOOK_URL env variable containing the discord webhook url
if [ "$#" -lt 5 ]; then
  echo "usage: discord_webhook.sh STATUS JOB_NAME BUILD_NUMBER REPO_URL CONTENT"
  echo " STATUS        = \"success\" or \"failure\". Will use \"Unknown\" when anything else is passed"
  echo " JOB_NAME      = name of the job EXACTLY as configured in Jenkins. Use quotes if the job name contains blanks"
  echo " BUILD_NUMBER  = jenkins build number, must be an integer"
  echo " REPO_URL      = URL to the (Github) repository"
  echo " VERSION       = [OPTIONAL] the version of the built component. Defaults to \"0.0.1-SNAPSHOT\""
  exit 1
fi

export WEBHOOK_URL_SAFE=$(echo "${WEBHOOK_URL}" | sed "s#webhooks/.*#webhooks/<masked_url>#g")

echo "'"WEBHOOK_URL:  "${WEBHOOK_URL_SAFE}""'"
echo "'"STATUS:       "${STATUS}""'"
echo "'"JENKINS_JOB:  "${JENKINS_JOB}""'"
echo "'"BUILD_NUMBER: "${BUILD_NUMBER}""'"
echo "'"REPO_URL:     "${REPO_URL}""'"
echo "'"VERSION:     "${VERSION}""'"
echo "'"CONTENT:      "${CONTENT}""'"


CI_PROVIDER="Jenkins"
DISCORD_AVATAR="https://wiki.jenkins.io/download/attachments/2916393/headshot.png?version=1&modificationDate=1302753947000&api=v2"
SUCCESS_AVATAR="https://jenkins.io/images/logos/cute/cute.png"
FAILURE_AVATAR="https://jenkins.io/images/logos/fire/fire.png"
UNKNOWN_AVATAR="https://www.jenkins.io/images/logos/mono/mono.png"

JOB_URL="https://ci.eclipse.org/edc/job/${JENKINS_JOB}"
BUILD_URL="${JOB_URL}/${BUILD_NUMBER}"
BUILD_URL="${BUILD_URL}/console"

echo
echo -e "[Webhook]: ${CI_PROVIDER} CI detected."
echo -e "[Webhook]: Sending webhook to Discord..."
echo

case ${STATUS} in
"SUCCESS")
  EMBED_COLOR=3066993
  STATUS_MESSAGE="Passed"
  AVATAR="${SUCCESS_AVATAR}"
  ;;
"FAILURE")
  EMBED_COLOR=15158332
  STATUS_MESSAGE="Failed"
  AVATAR="${FAILURE_AVATAR}"
  ;;
*)
  EMBED_COLOR=8421504
  STATUS_MESSAGE="Status Unknown: ${STATUS}"
  echo "status \"${STATUS}\" --> ${STATUS_MESSAGE}"
  AVATAR="${UNKNOWN_AVATAR}"
  ;;
esac

TIMESTAMP=$(date -u +%FT%TZ)
WEBHOOK_DATA='{
  "username": "Jenkins CI",
  "content": "'"${CONTENT}"'",
  "avatar_url": "'"${DISCORD_AVATAR}"'",
  "embeds": [ {
    "color": '${EMBED_COLOR}',
    "author": {
      "name": "'"${CI_PROVIDER}"' '"${JENKINS_JOB}  #${BUILD_NUMBER}"' - '"${STATUS_MESSAGE}"'",
      "url": "'"${BUILD_URL}"'",
      "icon_url": "'"${AVATAR}"'"
    },
    "title": "'"${JENKINS_JOB} - ${STATUS_MESSAGE}"'",
    "url": "'"${JOB_URL}"'",
    "fields": [
      {
        "name": "Job Name",
        "value": "'"[${JENKINS_JOB%}](${JOB_URL})"'",
        "inline": true
      },
      {
        "name": "Build Number",
        "value": "'"[${BUILD_NUMBER%.*}](${BUILD_URL})"'",
        "inline": true
      },
      {
        "name": "Branch/Tag",
        "value": "'"[\`${BRANCH_NAME}\`](${REPO_URL}/tree/${BRANCH_NAME})"'",
        "inline": true
      }
    ],
    "timestamp": "'"${TIMESTAMP}"'"
  } ]
}'

curl --fail --progress-bar -A "${CI_PROVIDER}-Webhook" -H "Content-Type:application/json" -d "${WEBHOOK_DATA}" ${WEBHOOK_URL}

if [ $? -ne 0 ]; then
  echo -e "Webhook data:\\n${WEBHOOK_DATA}"
  echo -e "\\n[Webhook]: Unable to send webhook."

  # Exit with an error signal
  exit 1
else
  echo -e "\\n[Webhook]: Successfully sent the webhook."
fi
