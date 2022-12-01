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

elif [ "$#" -eq 5 ]; then
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

curl --location --request POST "https://api.github.com/repos/$OWNER/$REPO/actions/workflows/$WORKFLOW/dispatches" \
  "${PARAMS[@]}" \
  --data-raw '{
      "ref": "main"
  }'
