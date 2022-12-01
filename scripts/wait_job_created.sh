#!/bin/bash

OWNER="$1"
REPO="$2"
WORKFLOW="$3"
USER="$4"
PWD="$5"

if [ "$#" -eq 4 ]; then
  # use cURL with a Personal Access Token
  echo "Using USER as personal access token for the GitHub API"
  PARAMS=(-H "Authorization: Bearer $USER" -H "Accept: application/vnd.github.v3+json")

elif
  [ "$#" -eq 5 ]
then
  # use basic auth with cUrl
  echo "Using USER/PWD authentication for the GitHub API"
  PARAMS=(-u "$USER":"$PWD" -H "Accept: application/vnd.github.v3+json")

else
  echo "Usage: start_build.sh OWNER REPO WORKFLOW USER [PWD]"
  echo "OWNER    = the owner/org of the github repo"
  echo "REPO     = the name of the github repo"
  echo "WORKFLOW = the name of the workflow file to run, or its ID"
  echo "USER     = the username to use for authentication against the GitHub API, or an API token"
  echo "PWD      = the password of USER. if not specified, USER will be interpreted as token"
  exit 1
fi

numRuns=0
echo "Waiting for run to start"
while [ "$numRuns" -le "0" ]; do
  sleep 3
  # fetch the latest run triggered by a workflow_dispatch event
  runs=$(curl --fail -sSl "${PARAMS[@]}" -X GET "https://api.github.com/repos/${OWNER}/${REPO}/actions/workflows/${WORKFLOW}/runs?event=workflow_dispatch&status=in_progress")
  numRuns=$(echo "$runs" | jq -r '.total_count')
  echo " - found $numRuns"
done

# contains the ID of the latest/most recent run
RUN_ID=$(echo "$runs" | jq -r '.workflow_runs[0].id')

echo "$RUN_ID" >run.id
