#!/bin/bash
set -e

taskrunner="${TASKRUNNER:-siglus/test-runner:1.0.0}"
pipelinename="${PIPELINE_NAME:-mobile_build}"
dockernet="${DOCKER_NET:-gocd_default}"
gradleexecAssembly="./gradlew assembleLocalDebug"
gradleexecLocal="./gradlew functionalTests"
gradleexecCi="./gradlew assembleLocalCi"
LOCAL_DOCKER_SCRIPT="sh  -c \"touch local.properties && $gradleexecAssembly && ./scripts/run_functional_tests.rb && rm local.properties\""
CI_DOCKER_SCRIPT="sh -c \"touch local.properties && ./scripts/run_functional_tests.rb && rm local.properties\""
LOCAL_SCRIPT="$gradleexecLocal"

. ./task_executor.sh
