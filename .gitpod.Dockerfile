FROM gitpod/workspace-full-vnc
SHELL ["/bin/bash", "-c"]

# install android sdk
USER root
ENV ANDROID_HOME=/home/gitpod/androidsdk \
    FLUTTER_VERSION=2.2.3-stable

# Install Open JDK
USER root
RUN apt update \
    && apt install openjdk-8-jdk -y \
    && update-java-alternatives --set java-1.8.0-openjdk-amd64

# Install SDK Manager commandlinetools
USER gitpod
RUN  wget https://dl.google.com/android/repository/commandlinetools-linux-7583922_latest.zip \
    && mkdir -p $ANDROID_HOME/cmdline-tools/latest \
    && unzip commandlinetools-linux-*.zip -d $ANDROID_HOME \
    && rm -f commandlinetools-linux-*.zip \
    && mv $ANDROID_HOME/cmdline-tools/bin $ANDROID_HOME/cmdline-tools/latest \
    && mv $ANDROID_HOME/cmdline-tools/lib $ANDROID_HOME/cmdline-tools/latest

RUN echo "export ANDROID_HOME=$ANDROID_HOME" >> /home/gitpod/.bashrc \
    && echo 'export PATH=$ANDROID_HOME/emulator:$ANDROID_HOME/tools:$ANDROID_HOME/cmdline-tools/bin:$ANDROID_HOME/platform-tools:$PATH' >> /home/gitpod/.bashrc

# Install Android Image version 30
USER gitpod
RUN yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-30"
RUN yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "system-images;android-30;google_apis;x86_64"
# RUN echo no | $ANDROID_HOME/cmdline-tools/latest/bin/avdmanager create avd -n avd28 -k "system-images;android-30;google_apis;x86_64"

# misc deps
RUN apt-get install -y \
    libasound2-dev \
    libgtk-3-dev \
    libnss3-dev \
    fonts-noto \
    fonts-noto-cjk

# For Qt WebEngine on docker
ENV QTWEBENGINE_DISABLE_SANDBOX 1
