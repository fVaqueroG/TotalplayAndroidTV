package com.fv.totalplaytv.ui;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Opt-in D-pad-to-touch adapter. Only acts while original Totalplay is in front.
 * Does not read/store/send text, passwords, account data, tokens or playback data.
 * Back, Home, volume, media and all other keys pass through unchanged.
 */
public final class TotalplayPointerService extends AccessibilityService {
    private static final String OFFICIAL = "com.TotalPlay.totalplay";
    private WindowManager windows;
    private View pointerOverlay;
    private boolean inTotalplay;
    private float x, y;
    private int width, height;

    @Override protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = getServiceInfo();
        if (info != null) {
            info.flags |= AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
            setServiceInfo(info);
        }
        windows = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        measureScreen();
        x = width / 2f;
        y = height / 2f;
        checkActiveWindow();
    }

    private void measureScreen() {
        if (windows == null) return;
        Point p = new Point();
        windows.getDefaultDisplay().getRealSize(p);
        width = Math.max(1, p.x);
        height = Math.max(1, p.y);
        x = Math.max(24f, Math.min(width - 24f, x));
        y = Math.max(24f, Math.min(height - 24f, y));
    }

    private void checkActiveWindow() {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null) {
            try {
                if (root.getPackageName() != null) {
                    setInTotalplay(OFFICIAL.contentEquals(root.getPackageName()));
                }
            } finally { root.recycle(); }
        }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        int type = event.getEventType();
        if (type != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                && type != AccessibilityEvent.TYPE_WINDOWS_CHANGED) return;
        CharSequence pkg = event.getPackageName();
        if (pkg == null || "android".contentEquals(pkg)
                || "com.android.systemui".contentEquals(pkg)) {
            checkActiveWindow();
        } else {
            setInTotalplay(OFFICIAL.contentEquals(pkg));
        }
    }

    private void setInTotalplay(boolean enabled) {
        if (inTotalplay == enabled) return;
        inTotalplay = enabled;
        if (enabled) showPointer(); else removePointer();
    }

    private void showPointer() {
        if (windows == null || pointerOverlay != null) return;
        measureScreen();
        pointerOverlay = new PointerView(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                android.graphics.PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        try { windows.addView(pointerOverlay, params); }
        catch (RuntimeException e) { pointerOverlay = null; }
    }

    private void removePointer() {
        if (windows == null || pointerOverlay == null) return;
        try { windows.removeView(pointerOverlay); }
        catch (RuntimeException ignored) { }
        pointerOverlay = null;
    }

    @Override protected boolean onKeyEvent(KeyEvent event) {
        int key = event.getKeyCode();
        boolean arrow = key == KeyEvent.KEYCODE_DPAD_UP
                || key == KeyEvent.KEYCODE_DPAD_DOWN
                || key == KeyEvent.KEYCODE_DPAD_LEFT
                || key == KeyEvent.KEYCODE_DPAD_RIGHT;
        boolean enter = key == KeyEvent.KEYCODE_DPAD_CENTER
                || key == KeyEvent.KEYCODE_ENTER
                || key == KeyEvent.KEYCODE_NUMPAD_ENTER;
        if (!arrow && !enter) return false;
        checkActiveWindow();
        if (!inTotalplay) return false;
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (arrow) {
                int step = event.getRepeatCount() > 5 ? 112 : 56;
                if (key == KeyEvent.KEYCODE_DPAD_UP) y -= step;
                if (key == KeyEvent.KEYCODE_DPAD_DOWN) y += step;
                if (key == KeyEvent.KEYCODE_DPAD_LEFT) x -= step;
                if (key == KeyEvent.KEYCODE_DPAD_RIGHT) x += step;
                x = Math.max(24f, Math.min(width - 24f, x));
                y = Math.max(24f, Math.min(height - 24f, y));
                if (pointerOverlay != null) pointerOverlay.invalidate();
            } else if (event.getRepeatCount() == 0) {
                Path tap = new Path();
                tap.moveTo(x, y);
                GestureDescription gesture = new GestureDescription.Builder()
                        .addStroke(new GestureDescription.StrokeDescription(tap, 0, 85)).build();
                dispatchGesture(gesture, null, null);
            }
        }
        return true;
    }

    @Override public void onInterrupt() { removePointer(); }
    @Override public void onDestroy() { removePointer(); windows = null; super.onDestroy(); }

    private final class PointerView extends View {
        private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
        PointerView(Context context) {
            super(context);
            ring.setColor(Color.rgb(55, 240, 169));
            ring.setStyle(Paint.Style.STROKE);
            ring.setStrokeWidth(5f);
            dot.setColor(Color.WHITE);
        }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawCircle(x, y, 21f, ring);
            canvas.drawCircle(x, y, 4f, dot);
        }
    }
}
