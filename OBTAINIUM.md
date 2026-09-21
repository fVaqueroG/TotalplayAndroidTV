# Install and update with Obtainium

Add `https://github.com/fVaqueroG/TotalplayAndroidTV` as a GitHub source in Obtainium and install the latest release APK. The companion now appears in the launcher as **Totalplay D-pad Pointer**; the APK asset retains the previous Totalplay-TV-Guide filename pattern for existing Obtainium configurations.

After updating, open the companion, select **Open Accessibility Settings**, and enable **Totalplay D-pad Pointer**. Go back and choose **Open original Totalplay**. The green pointer moves with the TV remote arrows, Enter/OK sends a simulated tap, and Back remains normal. The original app must remain installed and logged in on the same Android TV. Disable the pointer service in Android Accessibility settings to stop it. This prototype does not change the original app's portrait-oriented screen layout.

**Persistent updates:** Releases since `v0.1.9-prototype` use the private keystore configured in the repository's GitHub Actions secrets. If you installed a persistently signed version, updates to the same `com.fv.totalplaytv.ui` package should install in place. If you installed an earlier temporary debug-signed release, you may need to uninstall **only the companion** once; never uninstall the original Totalplay app to update this tool.

Keep your original private signing keystore/password backed up, and do not upload them to the public repository.

Releases: https://github.com/fVaqueroG/TotalplayAndroidTV/releases/latest
Builds: https://github.com/fVaqueroG/TotalplayAndroidTV/actions/workflows/build.yml
