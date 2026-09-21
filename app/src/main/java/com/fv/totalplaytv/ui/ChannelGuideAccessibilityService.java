package com.fv.totalplaytv.ui;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Experimental, user-enabled guide bridge. It never accesses passwords, cookies, token
 * stores, WebView internals, playback streams or the network. It only observes visible
 * channel-like labels on the original app's guide and can click a matching visible item.
 * Discovery is off unless the user explicitly starts it from the companion.
 */
public final class ChannelGuideAccessibilityService extends AccessibilityService {
    private static final Pattern INLINE_CHANNEL = Pattern.compile("^\\s*([0-9]{1,3})\\s*(?:[-–—:·|]\\s*|\\s{2,})([^\\n]{2,60})\\s*$");
    private static final Pattern NUMBER = Pattern.compile("^[0-9]{1,3}$");
    private long lastProbe;

    private static final class Label {
        final String value;
        final AccessibilityNodeInfo node;
        Label(String value, AccessibilityNodeInfo node) { this.value = value; this.node = node; }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null ||
                !MainActivity.OFFICIAL.contentEquals(event.getPackageName())) return;
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE);
        boolean scanning = prefs.getBoolean(MainActivity.SCANNING, false);
        String pending = prefs.getString(MainActivity.PENDING, "");
        if (!scanning && (pending == null || pending.isEmpty())) return;
        long now = SystemClock.uptimeMillis();
        if (now - lastProbe < 350) return;
        lastProbe = now;
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null || root.getPackageName() == null ||
                !MainActivity.OFFICIAL.contentEquals(root.getPackageName())) return;
        ArrayList<Label> labels = new ArrayList<>();
        boolean[] containsInput = {false};
        collect(root, labels, containsInput, 0);
        // An editable/password field indicates a possible sign-in/account screen. Capture nothing.
        if (containsInput[0] || labels.size() == 0) return;
        if (!onGuideScreen(labels)) return;
        if (scanning) scan(prefs, labels);
        if (pending != null && !pending.isEmpty()) selectVisibleChannel(prefs, labels, pending);
    }

    private void collect(AccessibilityNodeInfo node, List<Label> labels, boolean[] containsInput, int depth) {
        if (node == null || depth > 35 || labels.size() > 900) return;
        if (node.isPassword() || node.isEditable() ||
                "android.widget.EditText".contentEquals(node.getClassName() == null ? "" : node.getClassName())) {
            containsInput[0] = true;
            return;
        }
        CharSequence raw = node.getText();
        if (raw == null || raw.length() == 0) raw = node.getContentDescription();
        if (raw != null) {
            String value = raw.toString().trim();
            if (!value.isEmpty() && value.length() < 140) labels.add(new Label(value, node));
        }
        for (int i = 0; i < node.getChildCount() && labels.size() <= 900; i++) {
            collect(node.getChild(i), labels, containsInput, depth + 1);
        }
    }

    private boolean onGuideScreen(List<Label> labels) {
        for (Label label : labels) {
            String lower = label.value.toLowerCase(java.util.Locale.ROOT);
            if (lower.equals("canales") || lower.contains("guía de canales") ||
                lower.contains("guia de canales") || lower.equals("tv en vivo") ||
                lower.equals("televisión en vivo") || lower.equals("live tv") ||
                lower.equals("channel guide")) return true;
        }
        return false;
    }

    private static boolean channelName(String name) {
        if (name == null || name.length() < 2 || name.length() > 60) return false;
        if (!name.matches(".*[\\p{L}].*")) return false;
        String low = name.toLowerCase(java.util.Locale.ROOT);
        return !(low.contains("contraseña") || low.contains("password") || low.contains("sesión") ||
                low.contains("iniciar sesión") || low.contains("correo electrónico") || low.contains("perfil"));
    }

    private static void add(Map<String,String> catalog, String digits, String name) {
        int ch;
        try { ch = Integer.parseInt(digits); } catch (NumberFormatException ex) { return; }
        if (ch < 1 || ch > 999 || !channelName(name)) return;
        catalog.put(String.valueOf(ch), name.trim());
    }

    private void scan(SharedPreferences prefs, List<Label> labels) {
        LinkedHashMap<String,String> catalog = new LinkedHashMap<>();
        try {
            JSONArray previous = new JSONArray(prefs.getString(MainActivity.CHANNELS, "[]"));
            for (int i = 0; i < previous.length() && i < 400; i++) {
                JSONObject ch = previous.optJSONObject(i);
                if (ch != null) add(catalog, ch.optString("number"), ch.optString("name"));
            }
        } catch (Exception ignored) { }
        int before = catalog.size();
        for (int i = 0; i < labels.size() && catalog.size() < 400; i++) {
            Label current = labels.get(i);
            Matcher combined = INLINE_CHANNEL.matcher(current.value);
            if (combined.matches()) add(catalog, combined.group(1), combined.group(2));
            // Other layouts expose channel number and name as adjacent accessible nodes.
            if (NUMBER.matcher(current.value).matches() && i + 1 < labels.size()) {
                Label next = labels.get(i + 1);
                if (current.node.getParent() != null && next.node.getParent() != null &&
                        current.node.getParent().equals(next.node.getParent())) {
                    add(catalog, current.value, next.value);
                }
            }
        }
        if (catalog.size() == before) return;
        JSONArray output = new JSONArray();
        for (Map.Entry<String,String> entry : catalog.entrySet()) {
            JSONObject item = new JSONObject();
            try { item.put("number", entry.getKey()); item.put("name", entry.getValue()); }
            catch (Exception ignored) { continue; }
            output.put(item);
        }
        prefs.edit().putString(MainActivity.CHANNELS, output.toString())
                .putString(MainActivity.STATUS, "Observed " + catalog.size() + " channel-like entries in the official app. Check their accuracy before playback.").apply();
    }

    private void selectVisibleChannel(SharedPreferences prefs, List<Label> labels, String number) {
        for (Label label : labels) {
            if (!label.value.equals(number) &&
                    !label.value.matches("^\\s*" + Pattern.quote(number) + "\\s*[-–—:·| ]+.{2,60}$")) continue;
            AccessibilityNodeInfo item = label.node;
            for (int depth = 0; item != null && depth < 5; depth++, item = item.getParent()) {
                if (item.isClickable() && item.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    prefs.edit().putString(MainActivity.PENDING, "")
                        .putString(MainActivity.STATUS, "Selected visible channel " + number + " in the original Totalplay app. Playback is handled by that app; verify the result on your TV.").apply();
                    return;
                }
            }
        }
    }

    @Override public void onInterrupt() { }
    @Override public void onDestroy() {
        getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE).edit()
                .putBoolean(MainActivity.SCANNING, false).putString(MainActivity.PENDING, "").apply();
        super.onDestroy();
    }
}
