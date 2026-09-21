# Totalplay TV Guide — Android TV companion

An IPTV-style, remote-friendly Android app for controlling a compatible Totalplay decoder on your local network. Its **Tune decoder** and on-screen remote send real LAN remote commands. It also offers a button to launch the separate, original Totalplay Android app when installed. This app **does not** embed, extract, proxy or decrypt the official app's protected TV stream; tuning the decoder changes what plays **on that decoder's connected TV**, not inside this app or on the phone.

## Setup and use

1. Install the signed APK from [the latest GitHub Release](https://github.com/fVaqueroG/TotalplayAndroidTV/releases/latest), or add this repository to Obtainium (see [OBTAINIUM.md](OBTAINIUM.md)).
2. Keep your Android device (phone or TV) connected to a network that can reach the Totalplay decoder over LAN. Tap **⚙ Decoder**, enter the decoder's local IPv4 address (the initial default is `192.168.100.17`, but change it if yours differs), and tap **Save address**.
3. Reopen **⚙ Decoder** and tap **Test: open decoder guide**. This sends the `KEY_GUIDE` remote command; verify that the *decoder's TV* opens its guide. A successful HTTP response establishes that the endpoint accepted a request, **not** that the decoder actually switched channels or played video.
4. Choose one of the sample channels or enter any channel from your own subscribed lineup (1–999). Tap **Tune decoder** or **Go to channel**. The app sends a zero-padded three-digit channel number, with 100 ms between digits, waits 1.8 seconds by default (adjustable in Decoder settings), and then sends `ok`. Channels launching other decoder apps may need a longer pause.
5. Open **▦ Remote** for directional controls, Guide, Menu, Back, channel, volume and number-pad commands. The remote controls the decoder, **not** the separate Totalplay Android phone app.

**Channel guide:** The only preloaded entries are `655 — Noticiero` and `658 — Connie si prisa`, copied from the provided screenshot. You can add other channel numbers/names, saved locally on your device, and favorite them. These entries are not a live electronic program guide, a verified complete Totalplay channel lineup, or a promise that the channel is available under your subscription.

## How decoder control works

The app uses the LAN HTTP endpoint recovered from the official Totalplay remote-control application: `GET http://DECODER_IP/RemoteControl/KeyHandling/sendKey?key=...` on port 80. Requests run off the UI thread with timeouts; only private LAN IPv4 addresses and known remote keys are accepted. No Totalplay account credentials or streaming URLs are needed or stored. **This endpoint has not yet been verified against your own decoder with the newly built companion app.** If the guide test reports an error, confirm IP reachability and whether your particular decoder firmware exposes the endpoint. The connected TV may need to be on the decoder's HDMI input to see its channel change.

## Viewing on the Android TV itself

**↗ Totalplay app** opens the original, independently installed and signed app's *main screen*, not a specific channel. The original app's fullscreen player requires internal playback/session extras and is not an exported API; this companion cannot directly start or embed that player. Nothing in this repo is a replacement for the provider's login, entitlement checks, or original playback application. It does not fix crashes or sideways touch-only behavior of the provider's mobile app running on Android TV.

## Builds and signing

GitHub Actions builds a release APK on pushes to `main`, signs it with the same private PKCS12 key stored in repository Actions secrets `TOTALPLAY_KEYSTORE_B64` and `TOTALPLAY_KEYSTORE_PASSWORD`, verifies the APK signature, then uploads it to GitHub Releases for Obtainium. Both secrets are required; if missing or invalid, the workflow fails rather than publishing an APK signed with a throwaway key. Keep the original keystore/password privately backed up, and **never commit private keys, account credentials, vendor APKs, or sensitive logs** to this public repository.
