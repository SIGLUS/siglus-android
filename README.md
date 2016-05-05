# lmis-moz-mobile
Mozambique OpenLMIS Mobile

Unit Tests
--------------
run `./gradlew test` to run the Robolectric tests

Contract Tests
--------------
1. Install rvm via `\curl -L https://get.rvm.io | bash -s stable`
2. Install Ruby via: `rvm install ruby-2.1.1`
3. Install bundler via: `gem install bundler`
4. Run `./gradlew contractTests`

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
calabash-android run ../app/build/outputs/apk/app-dev-debug.apk --tags ~@Mmia
```