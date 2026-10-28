FROM gradle:8.13-jdk17

USER root

ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV PATH="${PATH}:/opt/android-sdk/cmdline-tools/latest/bin:/opt/android-sdk/platform-tools"

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl unzip \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p "${ANDROID_HOME}/cmdline-tools" \
    && curl -fsSL \
       https://dl.google.com/android/repository/commandlinetools-linux-15859902_latest.zip \
       -o /tmp/android-tools.zip \
    && echo "4e4c464f145a7512b57d088ac6c278c03c9eea610886b35a5e0804e74eedf583  /tmp/android-tools.zip" \
       | sha256sum -c - \
    && unzip -q /tmp/android-tools.zip -d "${ANDROID_HOME}/cmdline-tools" \
    && mv "${ANDROID_HOME}/cmdline-tools/cmdline-tools" \
          "${ANDROID_HOME}/cmdline-tools/latest" \
    && rm /tmp/android-tools.zip

RUN yes | sdkmanager --sdk_root="${ANDROID_HOME}" --licenses >/dev/null \
    && sdkmanager --sdk_root="${ANDROID_HOME}" \
       "platform-tools" \
       "platforms;android-35" \
       "build-tools;35.0.0" \
    && chown -R gradle:gradle "${ANDROID_HOME}"

USER gradle

WORKDIR /workspace

CMD ["gradle", "--no-daemon", "assembleDebug"]
