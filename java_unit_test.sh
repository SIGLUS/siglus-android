#!/bin/bash
set -e

taskrunner="${TASKRUNNER:-siglus/test-runner:1.0.0}"
pipelinename="${PIPELINE_NAME:-mobile_test_build}"
dockernet="${DOCKER_NET:-gocd_default}"
gradleexec="./gradlew clean testLocalDebug"
LOCAL_DOCKER_SCRIPT="docker run --rm -v $(pwd):/openlmis-mobile -v gradle_cache:/root/.gradle --name test_local_debug -e JAVA_TOOL_OPTIONS=\"-Xmx4096m -XX:MaxPermSize=512m -Xms128m\" -e JAVA_OPTS=\"-XX:MaxPermSize=512m\" --memory 4096m -w /openlmis-mobile $taskrunner $gradleexec"
CI_DOCKER_SCRIPT="docker run --rm --name test_local_debug --net $dockernet --volumes-from $HOSTNAME -w /godata/pipelines/$pipelinename $taskrunner $gradleexec"
LOCAL_SCRIPT="$gradleexec"

. ./task_executor.sh
