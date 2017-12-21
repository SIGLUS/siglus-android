#!/bin/bash
taskrunner="${TASKRUNNER:-siglus/test-runner:1.0.0}"
pipelinename="${PIPELINE_NAME:-mobile_build}"
dockernet="${DOCKER_NET:-gocd_default}"
gradleexec="./gradlew testLocalDebug"
LOCAL_DOCKER_SCRIPT="docker run --rm -v $(pwd):/openlmis-mobile -e JAVA_TOOL_OPTIONS=\"-Xmx2048m -XX:MaxPermSize=512m -Xms128m\" -e JAVA_OPTS=\"-XX:MaxPermSize=512m\" -w /openlmis-mobile $taskrunner $gradleexec"
CI_DOCKER_SCRIPT="docker run --rm --name test_local_debug --net $dockernet --volumes-from $HOSTNAME -w /godata/pipelines/$pipelinename $taskrunner $gradleexec"
LOCAL_SCRIPT="$gradleexec"

. ./task_executor.sh
