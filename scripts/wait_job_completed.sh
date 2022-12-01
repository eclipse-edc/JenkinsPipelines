#!/bin/bash

OWNER="$1"
REPO="$2"
RUN_ID="$3"
USER="$4"
PWD="$5"

if [ "$#" -eq 4 ]; then
  # use cURL with a Personal Access Token
  echo "Using USER as personal access token for the GitHub API"
  PARAMS=(-H "Authorization: Bearer $USER" -H "Accept: application/vnd.github.v3+json")
elif [ "$#" -eq 5 ]; then
  # use basic auth with cUrl
  echo "Using USER/PWD authentication for the GitHub API"
  PARAMS=(-u "$USER":"$PWD" -H "Accept: application/vnd.github.v3+json")
else
  echo "Usage: wait_job_completion.sh OWNER REPO RUN_ID USER [PWD]"
  echo "OWNER    = the owner/org of the github repo"
  echo "REPO     = the name of the github repo"
  echo "RUN_ID   = the ID of an (existing) workflow run"
  echo "USER     = the username to use for authentication against the GitHub API, or an API token"
  echo "PWD      = the password of USER. if not specified, USER will be interpreted as token"
  exit 1
fi

echo "Checking run ID $RUN_ID"
while [ "$status" != "completed" ]; do
  json=$(curl --fail -sSl "${PARAMS[@]}" -X GET "https://api.github.com/repos/${OWNER}/${REPO}/actions/runs/${RUN_ID}")
  status=$(echo "$json" | jq -r '.status')
  conclusion=$(echo "$json" | jq -r '.conclusion')
  echo "$(date) :: Run $RUN_ID is $status"
  sleep 5
done

echo "Run completed, conclusion: $conclusion"
