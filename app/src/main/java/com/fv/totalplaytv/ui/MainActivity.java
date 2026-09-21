package com.fv.totalplaytv.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/** IPTV-style guide and LAN remote. Playback stays in the official Totalplay app/decoder. */
public final class MainActivity extends Activity {
    private static final String PREFS = "totalplay-guide-settings";
    private static final String HOST = "decoder-ip";
    private static final String DEFAULT_HOST = "192.168.100.17";
    private static final Set<String> REMOTE_KEYS = new HashSet<>(Arrays.asList(
            "up", "down", "left", "right", "ok", "back", "KEY_MENU", "KEY_GUIDE",
            "channel_up", "channel_down", "volume_up", "volume_down", "mute",
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));
    private final ExecutorService remoteQueue = Executors.newSingleThreadExecutor();
    private final AtomicBoolean tuning = new AtomicBoolean(false);
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(5894 | 1024 | 512);
        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(false);
        webView.getSettings().setAllowFileAccessFromFileURLs(false);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(false);
        webView.addJavascriptInterface(new Bridge(), "AndroidBridge");
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v, String url) {
                return !url.startsWith("file:///android_asset/");
            }
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest request) {
                return !request.getUrl().toString().startsWith("file:///android_asset/");
            }
            @Override public void onPageFinished(WebView v, String url) {
                v.evaluateJavascript("document.querySelector('#categories button')?.focus()", null);
            }
        });
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        setContentView(webView);
        webView.loadUrl("file:///android_asset/index.html");
        webView.requestFocus();
    }

    private String decoderHost() {
        return getSharedPreferences(PREFS, MODE_PRIVATE).getString(HOST, DEFAULT_HOST);
    }

    // Keep cleartext traffic strictly on user-selected private LAN IPv4 addresses; never allow remote URLs.
    private static boolean privateIpv4(String host) {
        if (host == null || !host.matches("[0-9]{1,3}(\\.[0-9]{1,3}){3}")) return false;
        String[] segments = host.split("\\.");
        int[] ip = new int[4];
        for (int i = 0; i < 4; i++) {
            try { ip[i] = Integer.parseInt(segments[i]); }
            catch (NumberFormatException e) { return false; }
            if (ip[i] < 0 || ip[i] > 255) return false;
        }
        return ip[0] == 10 || (ip[0] == 172 && ip[1] >= 16 && ip[1] <= 31)
                || (ip[0] == 192 && ip[1] == 168);
    }

    private void sendKey(String host, String key) throws IOException {
        if (!privateIpv4(host) || !REMOTE_KEYS.contains(key)) throw new IOException("Invalid decoder address or remote key");
        String address = "http://" + host + "/RemoteControl/KeyHandling/sendKey?key="
                + URLEncoder.encode(key, "UTF-8");
        HttpURLConnection conn = (HttpURLConnection) new URL(address).openConnection();
        try {
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setUseCaches(false);
            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) throw new IOException("Decoder returned HTTP " + code);
        } finally { conn.disconnect(); }
    }

    private void report(String action, boolean ok, String message) {
        runOnUiThread(() -> {
            if (webView != null) webView.evaluateJavascript("window.__remoteResult && window.__remoteResult("
                    + JSONObject.quote(action) + "," + ok + "," + JSONObject.quote(message) + ")", null);
        });
    }

    public final class Bridge {
        @JavascriptInterface public String getDecoderIp() { return decoderHost(); }

        @JavascriptInterface public void saveDecoderIp(String input) {
            String ip = input == null ? "" : input.trim();
            if (!privateIpv4(ip)) {
                report("settings", false, "Enter a private LAN IPv4 address (for example 192.168.100.17)");
                return;
            }
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(HOST, ip).apply();
            report("settings", true, "Decoder address saved: " + ip);
        }

        @JavascriptInterface public void sendRemoteKey(String key) {
            if (key == null || !REMOTE_KEYS.contains(key)) {
                report("remote", false, "Unsupported remote key");
                return;
            }
            remoteQueue.execute(() -> {
                try {
                    sendKey(decoderHost(), key);
                    report("remote", true, "Sent " + key);
                } catch (Exception e) { report("remote", false, "Remote: " + e.getMessage()); }
            });
        }

        @JavascriptInterface public void testDecoder() {
            // The test opens the decoder's Guide: a visible, non-destructive acknowledgement.
            remoteQueue.execute(() -> {
                try {
                    sendKey(decoderHost(), "KEY_GUIDE");
                    report("test", true, "Guide command sent to decoder " + decoderHost());
                } catch (Exception e) { report("test", false, "Cannot reach decoder: " + e.getMessage()); }
            });
        }

        @JavascriptInterface public void tuneChannel(String value, int waitMs) {
            if (value == null || !value.matches("[0-9]{1,3}")) {
                report("tune", false, "Enter a channel number (1–999)");
                return;
            }
            final int number = Integer.parseInt(value);
            if (number < 1 || number > 999) {
                report("tune", false, "Channel number must be 1–999");
                return;
            }
            if (!tuning.compareAndSet(false, true)) {
                report("tune", false, "Another channel is still being selected");
                return;
            }
            final String digits = String.format(Locale.US, "%03d", number);
            final int pause = Math.max(500, Math.min(8000, waitMs));
            report("tune-start", true, "Selecting channel " + digits + " on the decoder");
            remoteQueue.execute(() -> {
                try {
                    String host = decoderHost();
                    for (int i = 0; i < digits.length(); i++) {
                        sendKey(host, digits.substring(i, i + 1));
                        if (i + 1 < digits.length()) Thread.sleep(100);
                    }
                    Thread.sleep(pause);
                    sendKey(host, "ok");
                    report("tune", true, "Channel " + digits + " sent to the decoder");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    report("tune", false, "Channel selection interrupted");
                } catch (Exception e) { report("tune", false, "Channel selection failed: " + e.getMessage()); }
                finally { tuning.set(false); }
            });
        }

        @JavascriptInterface public void openTotalplay() {
            runOnUiThread(() -> {
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.TotalPlay.totalplay");
                if (intent == null) {
                    Toast.makeText(MainActivity.this, "The official Totalplay app is not installed on this device", Toast.LENGTH_LONG).show();
                    return;
                }
                try { startActivity(intent); }
                catch (ActivityNotFoundException | SecurityException ex) {
                    Toast.makeText(MainActivity.this, "Cannot open the official Totalplay app", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override public void onBackPressed() {
        if (webView != null) webView.evaluateJavascript("window.__tvBack && window.__tvBack()", value -> {
            if (!"true".equals(value)) MainActivity.super.onBackPressed();
        });
        else super.onBackPressed();
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
            if (webView != null) webView.evaluateJavascript("document.getElementById('remoteToggle')?.focus()", null);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override public void onDestroy() {
        remoteQueue.shutdownNow();
        if (webView != null) {
            webView.removeJavascriptInterface("AndroidBridge");
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
