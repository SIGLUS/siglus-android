#Install F-droid

## 1. install docker
$ curl -sSL https://get.docker.com | sh

$ sudo docker run hello-world

$ sudo usermod -aG docker ubuntu

## 2. install nginx
$ docker pull nginx

## 3. install f-droid
$ docker pull gotsunami/fdroid

## 4. set up apk repository
$ sudo mkdir /app/appstore
$ sudo chown ubuntu:docker appstore
$ mkdir /app/appstore/repo

## 5. create sign key for appstore
$ keytool -genkey -v -keystore appstore.jks -alias clintonhealthaccess -keyalg RSA -keysize 2048 -validity 10000

## 6. set up appstore
$ sudo wget https://raw.githubusercontent.com/clintonhealthaccess/lmis-moz-mobile/master/appstore/appstore.jks
$ sudo wget https://raw.githubusercontent.com/clintonhealthaccess/lmis-moz-mobile/master/appstore/logo.png
$ sudo wget https://raw.githubusercontent.com/clintonhealthaccess/lmis-moz-mobile/master/appstore/config.py

## 7. put apks into repo directory
$ sudo cp /var/lib/jenkins/jobs/android-build/builds/lastSuccessfulBuild/archive/app/build/outputs/apk/app-qa-release.apk /app/appstore/repo/

## 8. start f-droid
$ export APP_STORE=/app/appstore

$ docker run —-rm -v $APP_STORE:/apk -v $APP_STORE/repo:/apk/repo gotsunami/fdroid

$ docker run -d —name nginx-fdroid -v $APP_STORE/repo:/usr/share/nginx/html:ro -p 9000:80 nginx


