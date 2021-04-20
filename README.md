# siglus-android
SIGLUS android app

Unit Tests
--------------
run `./gradlew testLocalDebug` to run the Robolectric tests for local debug apk

run `./java_unit_test.sh -d -e local` to run the unit tests in docker

Contract Tests
--------------
1. Install rvm via `\curl -L https://get.rvm.io | bash -s stable`
2. Install Ruby via: `rvm install ruby-2.1.1`
3. Install bundler via: `gem install bundler`
4. Install cucumber via: `gem install cucumber -v 1.3.20`
5. Install calabash-android via: `gem install calabash-android -v 0.9.0`
6. Run `./gradlew contractTests`

Functional Tests
--------------

Run all FunctionalTests

```
./gradlew assembleLocalDebug
./gradlew functionalTests
```

Run Specific Tag Functional Test

```
./gradlew assembleLocalDebug
cd functionalTests
calabash-android run ../app/build/outputs/apk/app-dev-debug.apk --tags @MMIA
```

Run all tests except specific tag

```
./gradlew assembleDevDebug
cd functionalTests
calabash-android run ../app/build/outputs/apk/app-dev-debug.apk --tags ~@MMIA
```

Running the training app
--------------


Run `./gradlew assembleShowCaseDebug` to package the showcase app
Run `./gradlew assembleTrainingDebug` to package the training app
You can find the generated apks under app/build/outputs/apk/.
Install them to Genymotion or your tablet and run.

If you want to package a signed release of the app:

You need to set environment variables for KSTOREPWD and KEYPWD.

After you have these environment variables set, run `./gradlew assembleShowCaseRelease` or `./gradlew assembleTrainingRelease`.


Code Coverage by Unit Tests
--------------
Run `./gradlew jacocoTestReport` in master branch.

you can get test coverage report in 'lmis-moz-mobile/app/build/reports'.

you can refrence "https://www.jacoco.org/jacoco/trunk/doc/counters.html" if you have  test report question.

