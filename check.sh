#!/bin/bash
./gradlew clean checkstyle pmd spotbugsLocalDebug testLocalDebug jacocoTestCoverageVerification
