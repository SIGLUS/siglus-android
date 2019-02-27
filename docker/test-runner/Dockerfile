FROM centos:7
MAINTAINER ThoughtWorks

RUN echo 'gem: --no-document' > /etc/gemrc

RUN yum -y update && yum -y install \
                device-mapper-persistent-data \
                epel-release \
                git \
                glibc.i686 \
                java-1.8.0-openjdk-devel \
                libstdc++.i686 \
                libffi \
                libffi-devel\
                lvm2 \
                make \
                mesa-libGL \
                openssl-devel\
                readline-devel\
                ruby-devel \
                unzip \
                wget \
                which \
                yum-config-manager \
                yum-utils \
                zlib-devel\
                zlib.i686

# install docker
RUN yum-config-manager --add-repo \
    https://download.docker.com/linux/centos/docker-ce.repo && \
    yum -y makecache fast && \
    yum -y install docker-ce

RUN yum -y groupinstall "Development Tools"

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
  echo "d56f5187479451eabf01fb78af6dfcb131a6481e" >> $ANDROID_HOME/licenses/android-sdk-license && \
  echo "84831b9409646a918e30573bab4c9c91346d8abd" > $ANDROID_HOME/licenses/android-sdk-preview-license && \
  mkdir ~/.android && \
  echo "count=0" > ~/.android/repositories.cfg

RUN yes | sdkmanager --licenses
RUN yes | sdkmanager "platforms;android-27"

RUN sdkmanager "tools" \
              "build-tools;27.0.3" \
              "platforms;android-21" \
              "platform-tools" \
              "extras;google;m2repository" \
              "extras;android;m2repository"
RUN sdkmanager --uninstall "patcher;v4" "emulator"

RUN git clone git://github.com/rbenv/rbenv.git /usr/local/rbenv \
&&  git clone git://github.com/rbenv/ruby-build.git /usr/local/rbenv/plugins/ruby-build \
&&  git clone git://github.com/jf/rbenv-gemset.git /usr/local/rbenv/plugins/rbenv-gemset \
&&  /usr/local/rbenv/plugins/ruby-build/install.sh
ENV PATH /usr/local/rbenv/bin:$PATH
ENV RBENV_ROOT /usr/local/rbenv

RUN echo 'export RBENV_ROOT=/usr/local/rbenv' >> /etc/profile.d/rbenv.sh \
&&  echo 'export PATH=/usr/local/rbenv/bin:$PATH' >> /etc/profile.d/rbenv.sh \
&&  echo 'eval "$(rbenv init -)"' >> /etc/profile.d/rbenv.sh

RUN echo 'export RBENV_ROOT=/usr/local/rbenv' >> /root/.bashrc \
&&  echo 'export PATH=/usr/local/rbenv/bin:$PATH' >> /root/.bashrc \
&&  echo 'eval "$(rbenv init -)"' >> /root/.bashrc

ENV CONFIGURE_OPTS --disable-install-doc
ENV PATH /usr/local/rbenv/bin:/usr/local/rbenv/shims:$PATH

RUN eval "$(rbenv init -)"; rbenv install 2.2.1 \
&&  eval "$(rbenv init -)"; rbenv global 2.2.1 \
&&  eval "$(rbenv init -)"; gem update --system \
&&  eval "$(rbenv init -)"; gem install bundle

RUN git clone https://github.com/SIGLUS/lmis-moz-mobile.git /lmis-mobile

COPY debug.keystore /root/.android/
RUN bundle config mirror.https://ruby.taobao.org https://rubygems.org
RUN cd /lmis-mobile && ./gradlew app:bundleInstall
RUN rm -rf /lmis-mobile

