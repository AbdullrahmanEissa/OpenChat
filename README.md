# OpenChat

OpenChat is a lightweight Android application that opens WhatsApp
conversations without requiring users to save phone numbers to contacts.

## Features

- Open WhatsApp chats using a phone number.
- Select from a small list of international calling codes.
- No account or backend required.
- No advertisements or analytics.
- No contacts permission.
- Built with Kotlin and native Android UI components.

## Requirements

- Docker
- Docker Compose
- A USB-connected Android device for direct installation (optional)

## Build

Build the Docker image:

```bash
docker compose build
```

Build the debug APK:

```bash
docker compose run --rm android-build
```

The APK is generated at:

`app/build/outputs/apk/debug/app-debug.apk`

## Install on a connected Android device

Make sure USB debugging is enabled and the device is authorized:

```bash
adb devices
adb install app/build/outputs/apk/debug/app-debug.apk
```

If a previously installed version has a different signing key, uninstall
that version before installing this debug build.

## Privacy

OpenChat does not need access to contacts or a backend service.
Opening a chat uses a WhatsApp web link. WhatsApp availability and its
own privacy practices are outside the control of this application.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).
