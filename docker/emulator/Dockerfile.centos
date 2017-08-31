FROM centos:7
MAINTAINER ThoughtWorks

EXPOSE 5554
EXPOSE 5555
EXPOSE 5901

RUN yum update && yum -y install \
                epel-release \
                qt5-qtbase-gui \
                qt5-qtsvg \
                java-1.8.0-openjdk \
                mesa-libGL \
                net-tools \
                socat \
                unzip \
                tigervnc-server \
                wget

#install android sdk tools
RUN wget -q  https://dl.google.com/android/repository/sdk-tools-linux-3859397.zip \
    && mkdir android-sdk/ \
    && unzip sdk-tools-linux-3859397.zip -d android-sdk/ \
    && rm sdk-tools-linux-3859397.zip \
    && mv android-sdk /usr/local/android-sdk \
    && chown -R root:root /usr/local/android-sdk/
#
#set enviroment variables
ENV ANDROID_HOME /usr/local/android-sdk
ENV PATH ${ANDROID_HOME}/emulator:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/tools/bin:$PATH
ENV JAVA_HOME /usr/lib/jvm/jre-1.8.0-openjdk
ENV LD_LIBRARY_PATH ${ANDROID_HOME}/emulator/lib64/qt/lib/

RUN ( sleep 4 && while [ 1 ]; do sleep 1; echo y; done ) | sdkmanager \
  "system-images;android-19;default;armeabi-v7a" \
  "platform-tools" \
  "platforms;android-19" \
  "emulator"
#
#create fake keymap file
RUN mkdir /usr/local/android-sdk/tools/keymaps && \
    touch /usr/local/android-sdk/tools/keymaps/en-us

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
