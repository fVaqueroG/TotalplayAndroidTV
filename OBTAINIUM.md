# Install and update with Obtainium

1. On your Android phone or Android TV, open Obtainium and choose **Add App**.
2. Enter `https://github.com/fVaqueroG/TotalplayAndroidTV` as the source. Select the GitHub source type if asked.
3. Install the latest `Totalplay-TV-Guide-*.apk` asset from **GitHub Releases**. The app's display name is **Totalplay TV Guide**. There is no need to download a workflow artifact ZIP.
4. Open **⚙ Decoder** in the app, confirm your Totalplay decoder's LAN IP address, save it, and use **Test: open decoder guide** to check the decoder's response. This app sends LAN remote commands to the physical decoder; it does not itself display the provider's live video.

**Persistent updates:** The current workflow requires the private keystore and password stored in GitHub Actions secrets; it verifies every release APK's signature. Obtainium can install subsequent releases in place, provided you retain **the same signing key and app package name**. If your installation was from an earlier temporary debug-signed prototype (before `v0.1.9-prototype`), uninstall **only Totalplay TV Guide** once before installing a persistently signed release; its signature cannot be changed in place. Do not remove the official Totalplay app. If you already installed `v0.1.9-prototype` or a later release signed using the private keystore, no signing-related reinstall should be needed.

**Protect your signing key:** keep a private copy of `totalplay-guide.p12` and its password. Never upload either to the public repo. Losing the signing key makes future compatible in-place updates impossible for users of that key.

Releases: https://github.com/fVaqueroG/TotalplayAndroidTV/releases/latest

Builds: https://github.com/fVaqueroG/TotalplayAndroidTV/actions/workflows/build.yml
