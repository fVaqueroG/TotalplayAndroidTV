package com.fv.totalplaytv.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityManager;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import org.json.JSONObject;
import java.util.List;

/** Experimental guide: sign-in/playback remain within the independently installed official app. */
public final class MainActivity extends Activity {
    static final String OFFICIAL = "com.TotalPlay.totalplay";
    static final String PREFS = "totalplay-guide-settings";
    static final String SCANNING = "scan-official-guide";
    static final String CHANNELS = "observed-official-channels";
    static final String PENDING = "pending-official-channel";
    static final String STATUS = "official-guide-status";
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(5894 | 1024 | 512);
        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowContentAccess(false);
        webView.getSettings().setAllowFileAccessFromFileURLs(false);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(false);
        webView.addJavascriptInterface(new Bridge(), "AndroidBridge");
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return !request.getUrl().toString().startsWith("file:///android_asset/");
            }
            @Override public void onPageFinished(WebView view, String url) { updateView(); }
        });
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        setContentView(webView);
        webView.loadUrl("file:///android_asset/index.html");
        webView.requestFocus();
    }

    private boolean isInstalled() {
        return getPackageManager().getLaunchIntentForPackage(OFFICIAL) != null;
    }
    private boolean accessibilityReady() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        if (am == null) return false;
        List<AccessibilityServiceInfo> enabled = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo info : enabled) {
            if (info.getResolveInfo() != null && info.getResolveInfo().serviceInfo != null &&
                getPackageName().equals(info.getResolveInfo().serviceInfo.packageName) &&
                ChannelGuideAccessibilityService.class.getName().equals(info.getResolveInfo().serviceInfo.name)) return true;
        }
        return false;
    }
    private void openOfficial() {
        Intent intent = getPackageManager().getLaunchIntentForPackage(OFFICIAL);
        if (intent == null) {
            Toast.makeText(this, "Install the original Totalplay app on this Android device first", Toast.LENGTH_LONG).show();
            return;
        }
        try { startActivity(intent); }
        catch (ActivityNotFoundException | SecurityException e) {
            Toast.makeText(this, "Could not open the original Totalplay app", Toast.LENGTH_LONG).show();
        }
    }
    private String stateJson() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("officialInstalled", isInstalled());
            obj.put("accessibilityReady", accessibilityReady());
            obj.put("channels", new org.json.JSONArray(getSharedPreferences(PREFS, MODE_PRIVATE).getString(CHANNELS, "[]")));
            obj.put("scanning", getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(SCANNING, false));
            obj.put("pending", getSharedPreferences(PREFS, MODE_PRIVATE).getString(PENDING, ""));
            obj.put("status", getSharedPreferences(PREFS, MODE_PRIVATE).getString(STATUS, "No channel data collected yet."));
            return obj.toString();
        } catch (Exception e) { return "{}"; }
    }
    private void updateView() {
        runOnUiThread(() -> {
            if (webView != null) webView.evaluateJavascript("window.__loadState && window.__loadState(" + stateJson() + ")", null);
        });
    }

    public final class Bridge {
        @JavascriptInterface public String getState() { return stateJson(); }
        @JavascriptInterface public void openSignIn() { runOnUiThread(() -> {
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(SCANNING, false)
                .putString(PENDING, "").putString(STATUS, "Sign in inside the original Totalplay app; passwords are never entered into this guide.").apply();
            openOfficial();
        }); }
        @JavascriptInterface public void openAccessibilitySettings() { runOnUiThread(() -> {
            try { startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); }
            catch (Exception e) { Toast.makeText(MainActivity.this, "Open Android Settings → Accessibility", Toast.LENGTH_LONG).show(); }
        }); }
        @JavascriptInterface public void scanOfficialGuide() { runOnUiThread(() -> {
            if (!isInstalled() || !accessibilityReady()) {
                Toast.makeText(MainActivity.this, "Install Totalplay and enable TV Guide Discovery in Accessibility first", Toast.LENGTH_LONG).show();
                updateView(); return;
            }
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(SCANNING, true)
                    .putString(PENDING, "")
                    .putString(STATUS, "Navigate to Live TV / channel guide inside Totalplay and scroll through the channel list. Return here to view detected entries.").apply();
            openOfficial();
        }); }
        @JavascriptInterface public void stopScan() { runOnUiThread(() -> {
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(SCANNING, false).apply();
            updateView();
        }); }
        @JavascriptInterface public void refresh() { updateView(); }
        @JavascriptInterface public void playObservedChannel(String number) { runOnUiThread(() -> {
            if (number == null || !number.matches("[0-9]{1,3}") || !accessibilityReady()) {
                Toast.makeText(MainActivity.this, "Enable Accessibility and select a discovered channel", Toast.LENGTH_LONG).show(); return;
            }
            // Match only observed channels. Never send untrusted intents or private playback tokens.
            String json = getSharedPreferences(PREFS, MODE_PRIVATE).getString(CHANNELS, "[]");
            boolean known = false;
            try {
                org.json.JSONArray observed = new org.json.JSONArray(json);
                for (int i = 0; i < observed.length(); i++) {
                    if (number.equals(observed.getJSONObject(i).optString("number"))) { known = true; break; }
                }
            } catch (Exception ignored) { }
            if (!known) { Toast.makeText(MainActivity.this, "Channel has not been observed in the official guide", Toast.LENGTH_LONG).show(); return; }
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(SCANNING, false)
                .putString(PENDING, number)
                .putString(STATUS, "Find channel " + number + " in the official app's guide. TV Guide Discovery will try to select its visible entry.").apply();
            openOfficial();
        }); }
    }
    @Override protected void onResume() { super.onResume(); updateView(); }
    @Override public void onBackPressed() {
        if (webView != null) webView.evaluateJavascript("window.__tvBack && window.__tvBack()", result -> {
            if (!"true".equals(result)) MainActivity.super.onBackPressed();
        });
        else super.onBackPressed();
    }
    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
            if (webView != null) webView.evaluateJavascript("document.getElementById('connect')?.focus()", null);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
    @Override protected void onDestroy() {
        if (webView != null) { webView.removeJavascriptInterface("AndroidBridge"); webView.destroy(); webView = null; }
        super.onDestroy();
    }
}
