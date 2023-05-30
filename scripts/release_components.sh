#!/bin/bash

VERSION="$1"
BOT="$2"
BOTTOKEN="$3"

set -eu

# Run workflows in parallel

pids=()

./scripts/github_action.sh "eclipse-edc" "gradleplugins" "release-all-java.yml" "{\"metamodel_version\", \"${VERSION}\"}" $BOT $BOTTOKEN &
pids+=($!)

./scripts/github_action.sh "eclipse-edc" "connector" "release-edc.yml" "{\"edc_version\", \"${VERSION}\"}" $BOT $BOTTOKEN &
pids+=($!)

./scripts/github_action.sh "eclipse-edc" "identityhub" "release-identityhub.yml" "{\"ih_version\", \"${VERSION}\"}" $BOT $BOTTOKEN &
pids+=($!)

./scripts/github_action.sh "eclipse-edc" "registrationservice" "release-registrationservice.yml" "{\"rs_version\", \"${VERSION}\"}" $BOT $BOTTOKEN &
pids+=($!)

./scripts/github_action.sh "eclipse-edc" "federatedcatalog" "release-fcc.yml" "{\"edc_version\", \"${VERSION}\"}" $BOT $BOTTOKEN &
pids+=($!)

# Wait for workflow completion, if any of them fails, the script will fail.
for pid in "${pids[@]}"; do
  wait "$pid"
done
