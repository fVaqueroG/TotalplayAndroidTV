package com.fv.totalplaytv.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/** Companion UI ONLY. Uses original signed Totalplay app for authorized playback. */
public final class MainActivity extends Activity {
    private WebView webView;
    @SuppressLint("SetJavaScriptEnabled")
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(5894 | 1024 | 512);
        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(false);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(false);
        webView.addJavascriptInterface(new Bridge(), "AndroidBridge");
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v, String url) {
                return !url.startsWith("file:///android_asset/");
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
    public final class Bridge {
        @JavascriptInterface public void openTotalplay() {
            runOnUiThread(() -> {
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.TotalPlay.totalplay");
                if (intent == null) {
                    Toast.makeText(MainActivity.this, "Install the original Totalplay app first", Toast.LENGTH_LONG).show();
                    return;
                }
                try { startActivity(intent); }
                catch (ActivityNotFoundException | SecurityException ex) {
                    Toast.makeText(MainActivity.this, "Cannot open the original Totalplay app", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    @Override public void onBackPressed() {
        // The WebView's own history is never used. Back leaves this prototype.
        if (webView != null) webView.evaluateJavascript("window.__tvBack && window.__tvBack()", value -> {
            if (!"true".equals(value)) MainActivity.super.onBackPressed();
        });
        else super.onBackPressed();
    }
    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_MENU){
            if(webView!=null)webView.evaluateJavascript("document.getElementById('openApp').focus()", null);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
    @Override public void onDestroy() {
        if(webView != null){webView.removeJavascriptInterface("AndroidBridge");webView.destroy();webView=null;}
        super.onDestroy();
    }
}
