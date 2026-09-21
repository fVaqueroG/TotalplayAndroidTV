# Independent Totalplay TV+ login: APK investigation

Status: **NOT IMPLEMENTED / NOT VERIFIED**. This is a technical research note, not a working authentication specification. Do not publish a login screen, ask for credentials, or label this app signed-in until the requirements below are met.

## User requirement

The Android TV app must log in independently, retrieve channels available to the account and play permitted channels on the Android TV. The separately installed original app must not be required; decoder LAN remote control is explicitly out of scope.

## Inputs examined

- The user-provided original Android viewing APK, version 4.1.420, package `com.TotalPlay.totalplay` (not the separate Totalplay Control APK).
- The user's ADB activity trace, which confirms that successful sign-in in the original app transitions into `mx.com.totalplay.core41.home.HomeActivity`. This confirms original-app login only, not standalone app access.
- The user's UIAutomator guide XML: the original guide exposes some visible category labels, channel numbers and program details, but not authentication requests or a complete account-specific catalog.

## Verified static code structure (original APK)

- `LOKL/mx/com/totalplay/core5/login/auth/LoginAuthAPI;`: `fetchToken()` and `fetchTokenSync()` use a Volley request. The latter constructs an HTTP Basic authentication header and consumes an `accessToken` from the response. **Do not extract, commit, print, or reuse the original application's embedded client credentials.**
- `LOKL/mx/com/totalplay/core5/login/repository/NewLoginRepository;`: `passwordLogin(...)` delegates to `doLogin(...)`. The `doLogin` path uses `CryptoClass.encrypt(...)`, constructs a login request through `createNewLoginRequest(...)`, queues it via Volley, and interacts with `SecurePreferences`. This is more than a plain username-and-password POST.
- The `createNewLoginRequest(...)` request builder reads a value from Android `Settings.Secure` and builds a request with device-related information. A third-party Android TV package cannot assume that a request copied from the original app will be accepted.
- `LOKL/mx/com/totalplay/core5/live/webservices/LiveTvApi;` has methods for categories, EPG-related information, session-dependent configuration and encryption-key retrieval. This does **not** establish that these services accept an independently authenticated client or that video playback can run in this app.

## Not verified

- A documented, supported third-party authentication endpoint, client registration or OAuth/SDK flow for this project.
- Whether Totalplay allows this separately signed Android TV app to register/authorize a device, or whether its service restricts client identities.
- A complete, safe login request/response contract, any verification factors, a server-accepted session, token refresh, or logout.
- An authenticated channel-catalog response from **this app's own session**, or authorized playback metadata from a selected channel.
- DRM/player/device compatibility on this Android TV.

## Implementation gates

1. Establish a provider-accepted authentication method for this independent client; do not transplant vendor client secrets, disable security checks or obtain tokens from the installed original app. Protect credentials on-device, avoid network logging of secrets, and complete any user-visible verification.
2. Confirm a server-accepted session in the companion package and a successful fetch of the real account-scoped channel catalog. Make failures visible; never fall back to hardcoded channels.
3. Verify authorized playback of one permitted channel on Android TV, including any legitimate player/DRM/device constraints.
4. Only then implement the full D-pad IPTV interface and release a genuinely working APK via Obtainium.

No original APK, decrypted secrets, account details, auth headers, personal tokens, DRM keys, proprietary decompiled source, or login recordings are included in this repository. This research branch deliberately does not trigger another prototype release from `main`.
