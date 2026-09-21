# Totalplay TV Guide — Android TV UI prototype

A landscape, D-pad-oriented companion user interface with an IPTV-style guide. **It is NOT a modified Totalplay application and does NOT play, extract, proxy, decrypt, or retune Totalplay streams.** The original signed Totalplay mobile APK stays installed independently and must be used for authorized viewing. The companion's "Open Totalplay" button opens that app's **main screen** (the TV deep link is not confirmed to open the guide).

This project is a UI implementation/prototype, not a replacement for a functional TV-only player. Its preview area intentionally displays a clear placeholder, not a fake video; category information and the two example channel entries (655 and 658) came from the supplied tablet screenshot. No other channel lineup or live EPG is claimed to be available. The favorites setting applies only to this prototype.

## What works

- Landscape, Leanback-launchable Android Activity with remote-focusable buttons and a two-pane IPTV-style guide.
- Browse, category selection, search, selection, favorites, fullscreen UI preview and Back handling.
- Open the original Totalplay app using its ordinary launcher intent (not a direct-to-channel intent).
- Browser preview at `app/src/main/assets/index.html` (open locally to inspect the UI; browser mode cannot launch Android apps).

## Building an installable prototype

Open this directory in Android Studio; let it download Gradle and Android SDK 35, then select **Build > Build APK(s)**. Or put this project in a GitHub repository, run the included GitHub Actions workflow, and download its `tv-ui-debug-apk` artifact. The resulting APK can be sideloaded to an Android TV with `adb install -r app-debug.apk`. No APK was built or verified in this environment because the Android SDK and Gradle are unavailable here.

**Important:** You must *not* uninstall the official Totalplay APK or replace its package/signature with this prototype. This does not solve the observed crashes in the original mobile app on Android TV.

## Real live TV integration requirement

The original mobile application's fullscreen player is private (non-exported) and receives internal extras with playback context. Its `/section` link was tested and opened the **main screen**, not live TV. A working TV-only player would need an officially supported playback handoff/API or a verified modification to the original application's own UI without breaking its login, signing, or authorization. This source code deliberately does not bypass those restrictions or pretend to have a playable channel integration.

## Publish to your repository

The repository is intended for `https://github.com/fVaqueroG/TotalplayAndroidTV`.
Unzip the GitHub-ready archive **into the root of a fresh clone** of that repository, not into a second nested project folder. Commit and push the source. GitHub Actions will run the prototype debug build on a push to `main` or through **Actions → Build Android TV UI prototype → Run workflow**. Download the `tv-ui-debug-apk` workflow artifact, extract the APK and sideload it with ADB.

The project does not contain the original Totalplay APK, a channel stream, encryption keys, or any account credentials. Do not upload the vendor APK, passwords, private stream URLs, or Android log files containing authentication information to this public repository.
