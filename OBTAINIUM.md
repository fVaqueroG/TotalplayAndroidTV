# Install and update with Obtainium

Add `https://github.com/fVaqueroG/TotalplayAndroidTV` as a GitHub source in Obtainium, then select the latest GitHub Release APK.

**Current experiment:** The app opens the independently installed official Totalplay app so you can sign in there. It does not itself sign in, receive Totalplay's account session, show your account's TV channel list, or play TV channels. The previous decoder remote is not part of the current app. Keep the original Totalplay app installed on the same Android device.

**Persistent updates:** APKs released since `v0.1.9-prototype` use the private signing keystore stored in GitHub Actions secrets. If you installed one of those signed releases, subsequent versions using the same key and package ID can be installed in place. If your installation was from an earlier temporarily debug-signed release, you may need to uninstall **only the companion** once and reinstall; do not uninstall the original Totalplay app.

Keep your original private signing keystore/password backed up. Do not upload them to the public repository.

Releases: https://github.com/fVaqueroG/TotalplayAndroidTV/releases/latest
Builds: https://github.com/fVaqueroG/TotalplayAndroidTV/actions/workflows/build.yml
