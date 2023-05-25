#!/bin/bash

BOT="$1"
BOTTOKEN="$2"

set -eu

# Run workflows in parallel

pids=()

./scripts/github_action.sh "eclipse-edc" "gradleplugins" "test.yaml" "" $BOT $BOTTOKEN &
pids+=($!)

./scripts/github_action.sh "eclipse-edc" "connector" "verify.yaml" "" $BOT $BOTTOKEN &
pids+=($!)

./scripts/github_action.sh "eclipse-edc" "identityhub" "verify.yaml" "" $BOT $BOTTOKEN &
pids+=($!)

./scripts/github_action.sh "eclipse-edc" "registrationservice" "verify.yaml" "" $BOT $BOTTOKEN &
pids+=($!)

./scripts/github_action.sh "eclipse-edc" "federatedcatalog" "verify.yaml" "" $BOT $BOTTOKEN &
pids+=($!)

# Wait worfklows completion, if any of them fail, the script will fail.
for pid in "${pids[@]}"; do
  wait "$pid"
done
