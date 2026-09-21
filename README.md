# Totalplay Android TV — sign-in experiment

This repository currently builds a **limited, experimental companion** for Android TV. The app has one functioning sign-in action: it opens the independently installed, original signed Totalplay Android app, where you can sign in using Totalplay's own interface. The companion does not accept, collect, store or transmit account credentials, passwords or authentication tokens. It does **not** log in by itself, receive the official app's session, fetch your subscribed channel lineup, or play live video.

## What to test

1. Install the latest persistently signed APK through Obtainium or [GitHub Releases](https://github.com/fVaqueroG/TotalplayAndroidTV/releases/latest).
2. Install the **original Totalplay app** independently on the **same Android device**. Do not replace the original package or signing certificate with this companion.
3. Open the companion and select **OPEN OFFICIAL TOTALPLAY / SIGN IN**. Confirm whether the original app launches and allows you to sign in. If it crashes on Android TV, this prototype does not correct the crash.

This is **not yet the simplified IPTV guide requested by the project owner**. It deliberately does not fabricate an account-specific channel list or claim that the vendor's authentication works in our separate APK. The original APK's login repository uses its own app-internal session handling, and the APK does not expose a login-result interface our separately signed package can just call. A functional standalone experience requires a verified and supported sign-in/session integration as well as the catalog and authorized playback flow.

The previous decoder LAN remote was removed from the app's entry screen; controlling a set-top box is not the goal of this project. The previous HTML demo at `app/src/main/assets/index.html` is not the current native app UI and is not connected to a real account.

## Builds and signing

GitHub Actions builds, signs and publishes the APK on pushes to `main`. The workflow uses the privately configured `TOTALPLAY_KEYSTORE_B64` and `TOTALPLAY_KEYSTORE_PASSWORD` repository secrets and verifies the release signature. The official Totalplay APK, account secrets and playback tokens must **not** be committed to this public repository.
