FROM centos:7
MAINTAINER ThoughtWorks

RUN echo 'gem: --no-document' > /etc/gemrc

RUN yum update && yum -y install \
                epel-release \
                git \
                glibc.i686 \
                java-1.8.0-openjdk-devel \
                libstdc++.i686 \
                make \
                mesa-libGL \
                ruby \
                ruby-devel \
                unzip \
                wget \
                zlib.i686

RUN yum groupinstall "Development Tools" -y

# install android sdk tools
RUN wget -q  https://dl.google.com/android/repository/sdk-tools-linux-3859397.zip \
    && mkdir android-sdk/ \
    && unzip sdk-tools-linux-3859397.zip -d android-sdk/ \
    && rm sdk-tools-linux-3859397.zip \
    && mv android-sdk /usr/local/android-sdk \
    && chown -R root:root /usr/local/android-sdk/

# set enviroment variables
ENV ANDROID_HOME /usr/local/android-sdk
ENV PATH ${ANDROID_HOME}/emulator:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/tools/bin:$PATH
ENV JAVA_HOME /usr/lib/jvm/jre-1.8.0-openjdk

# prepare sdkmanager
RUN mkdir -p $ANDROID_HOME/licenses/ && \
  echo "8933bad161af4178b1185d1a37fbf41ea5269c55" > $ANDROID_HOME/licenses/android-sdk-license && \
  echo "84831b9409646a918e30573bab4c9c91346d8abd" > $ANDROID_HOME/licenses/android-sdk-preview-license && \
  mkdir ~/.android && \
  echo "count=0" > ~/.android/repositories.cfg

RUN sdkmanager "tools" \
              "build-tools;22.0.1" \
              "platforms;android-21" \
              "platform-tools" \
              "extras;google;m2repository" \
              "extras;android;m2repository"
RUN sdkmanager --uninstall "patcher;v4" "emulator"

RUN git clone https://github.com/SIGLUS/lmis-moz-mobile.git /lmis-mobile
RUN gem install bundler
COPY debug.keystore /root/.android/
RUN cd /lmis-mobile && ./gradlew app:bundleInstall
