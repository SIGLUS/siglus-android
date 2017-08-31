FROM alpine:3.6
MAINTAINER ThoughtWorks
ENV GLIBC_VERSION "2.25-r0"

# RUN echo "http://dl-2.alpinelinux.org/alpine/edge/community" >> /etc/apk/repositories; \
#     echo "http://dl-3.alpinelinux.org/alpine/edge/community" >> /etc/apk/repositories; \
#     echo "http://dl-4.alpinelinux.org/alpine/edge/community" >> /etc/apk/repositories; \
#     echo "http://dl-5.alpinelinux.org/alpine/edge/community" >> /etc/apk/repositories

RUN apk --update add bash \
                      bc \
                      gawk \
                      grep \
                      libstdc++ \
                      openjdk8-jre \
                      openssl \
                      sed && \
                      update-ca-certificates

# install glibc for alpine
RUN wget https://raw.githubusercontent.com/sgerrand/alpine-pkg-glibc/master/sgerrand.rsa.pub -O /etc/apk/keys/sgerrand.rsa.pub \
  && wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/$GLIBC_VERSION/glibc-$GLIBC_VERSION.apk -O /tmp/glibc.apk \
  && wget https://github.com/sgerrand/alpine-pkg-glibc/releases/download/$GLIBC_VERSION/glibc-bin-$GLIBC_VERSION.apk -O /tmp/glibc-bin.apk \
  && apk add --no-cache /tmp/glibc.apk /tmp/glibc-bin.apk \
  && rm  -rf /tmp/* \
  && rm -rf /var/cache/apk/*

# install android sdk
RUN wget -q  https://dl.google.com/android/repository/sdk-tools-linux-3859397.zip \
    && mkdir android-sdk/ \
    && unzip sdk-tools-linux-3859397.zip -d android-sdk/ \
    && rm sdk-tools-linux-3859397.zip \
    && mv android-sdk /usr/local/android-sdk \
    && chown -R root:root /usr/local/android-sdk/

# set enviroment variables
ENV ANDROID_HOME /usr/local/android-sdk
ENV PATH ${ANDROID_HOME}/emulator:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/tools/bin:$PATH
ENV USE_SDK_WRAPPER true

RUN mkdir -p /root/.android/ \
  && touch /root/.android/repositories.cfg

RUN ( sleep 4 && while [ 1 ]; do sleep 1; echo y; done ) | sdkmanager \
  "system-images;android-19;default;x86" \
  "platform-tools" \
  "platforms;android-19" \
  "emulator"

# Create fake keymap file
RUN mkdir /usr/local/android-sdk/tools/keymaps && \
    touch /usr/local/android-sdk/tools/keymaps/en-us

CMD ["/usr/bin/java", "-version"]
