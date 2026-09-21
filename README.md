# Totalplay Android TV — D-pad pointer prototype

This Android TV **companion** makes the independently installed, original signed Totalplay app usable with a D-pad without touching the provider's authentication, channel-list or video player. It is **not** an independent Totalplay client, APK modification, IPTV player or decoder remote. The original app must be installed on the same Android TV device.

## Install and test

1. Update the companion through [GitHub Releases](https://github.com/fVaqueroG/TotalplayAndroidTV/releases/latest) / Obtainium. Keep the **original Totalplay app** installed, signed and logged in as before.
2. Launch **Totalplay D-pad Pointer** and open **Android Accessibility settings** with its first button. Locate **Totalplay D-pad Pointer** under installed/downloaded services and enable it. Android may show a broad accessibility-permission warning: this is a system-level permission that can observe the active app and filter remote keys. Enable it only if you are comfortable with that permission; you can disable it again from the same menu.
3. Return to the companion and select **Open original Totalplay**. In the original app, a green ring should appear near screen center.
4. Arrows move the green ring in increments; hold them for faster movement. Enter/OK sends a simulated tap. **Back, Home, volume, and other buttons retain their existing behavior**. For a first test, point at a harmless button in the original app and press Enter.

Only when the original package `com.TotalPlay.totalplay` is active will the service translate arrows/Enter; it does not attempt to navigate or tap any other apps. It does not read, record, store, or transmit passwords, text, account/session data, playback URLs or channel information. It requests accessibility window-content access only to recognize the foreground app, key filtering for arrows/Enter, and gesture dispatch for taps. The implementation does not request Internet access or any account login details. If the TV firmware disallows accessibility key filtering, the pointer may appear while arrows still fail to move it; this requires testing on the target TV.

## Limitations

- This version is a **remote pointer**, not semantic channel-to-channel navigation. It cannot yet scroll a touch-only guide, drag sliders, or reshape the original UI.
- The existing Totalplay app retains its own portrait-oriented layouts; the companion does **not** make it a true 16:9 responsive TV app. Test navigation first and adjust screen layout as a separate project.
- Accessibility overlay/gesture permissions may be restricted by some TV vendors. A simulated ADB tap working verifies the app receives touch input; it does not prove the accessibility service can capture D-pad events on your particular firmware.
- Do not replace or re-sign the original APK: that could invalidate its working login and video playback.

## Build and update

GitHub Actions signs releases with the persistent repository signing key. The companion keeps its original Android package ID `com.fv.totalplaytv.ui` so Obtainium can update existing signed companion installations. Never commit the original Totalplay APK, passwords, vendor client credentials or session tokens to this public repository.
