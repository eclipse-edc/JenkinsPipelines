#!/bin/bash

VERSION="$1"
BOT="$2"
BOTTOKEN="$3"

set -eu

# Run workflows in parallel

pids=()

./scripts/github_action.sh "eclipse-edc" "gradleplugins" "release-all-java.yaml" "{\"metamodel_version\", \"${VERSION}\"}" $BOT $BOTTOKEN &
pids+=($!)

./scripts/github_action.sh "eclipse-edc" "connector" "release-edc.yaml" "{\"edc_version\", \"${VERSION}\"}" $BOT $BOTTOKEN &
pids+=($!)

./scripts/github_action.sh "eclipse-edc" "identityhub" "release-identityhub.yaml" "{\"ih_version\", \"${VERSION}\"}" $BOT $BOTTOKEN &
pids+=($!)

./scripts/github_action.sh "eclipse-edc" "registrationservice" "release-registrationservice.yaml" "{\"rs_version\", \"${VERSION}\"}" $BOT $BOTTOKEN &
pids+=($!)

./scripts/github_action.sh "eclipse-edc" "federatedcatalog" "release-fcc.yaml" "{\"edc_version\", \"${VERSION}\"}" $BOT $BOTTOKEN &
pids+=($!)

# Wait for workflow completion, if any of them fails, the script will fail.
for pid in "${pids[@]}"; do
  wait "$pid"
done
