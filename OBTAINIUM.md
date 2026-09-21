# Install through Obtainium

This repository builds an Android TV UI companion prototype, not a replacement for the official Totalplay player. The prototype guide does not play live channels. Keep the original Totalplay application installed.

1. On your Android TV open Obtainium and select Add App.
2. Enter `https://github.com/fVaqueroG/TotalplayAndroidTV` as its GitHub source.
3. Add the app, then install the latest `Totalplay-TV-Guide-*.apk` GitHub Release asset. Approve installation permission for Obtainium if prompted.

The app appears as **Totalplay TV Guide (prototype)** on Android TV. You do not need to install an Actions artifact ZIP; Obtainium uses the APK attached to the GitHub Release.

## Important: signing and updates

The current workflow generates a debug-signed prototype APK using a temporary key on each GitHub Actions runner. Version codes increase between builds, but Android can reject an in-place update signed by a different key even if Obtainium detects the update. If that happens, uninstall **only** the Totalplay TV Guide prototype and install its newer APK. Do not uninstall the official Totalplay application.

Reliable future in-place updates require a persistent private signing keystore stored in GitHub Actions secrets and reused for each release. Switching from the temporary debug signing key to a persistent key also requires one reinstall. Never commit a private keystore, passwords, or Totalplay credentials to this public repository.

Releases: https://github.com/fVaqueroG/TotalplayAndroidTV/releases

Builds: https://github.com/fVaqueroG/TotalplayAndroidTV/actions/workflows/build.yml
