#! /usr/bin/env bash

set -eu

pids=()

for x in 1 2 3; do
  ls /not-a-file &
  pids+=($!)
done

for pid in "${pids[@]}"; do
  wait "$pid"
done
