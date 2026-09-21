package com.fv.totalplaytv.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** Setup activity only. Authentication, guide and playback stay in the signed original app. */
public final class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setPadding(48, 32, 48, 32);
        root.setBackgroundColor(0xff0b1425);
        TextView title = new TextView(this);
        title.setText("Totalplay • D-pad pointer test");
        title.setTextSize(30);
        title.setTextColor(0xffffffff);
        root.addView(title);
        TextView directions = new TextView(this);
        directions.setText("1. Enable ‘Totalplay D-pad Pointer’ in Android Accessibility settings.\n\n"
                + "2. Open the original Totalplay app using the button below.\n\n"
                + "3. Arrows move the green pointer (hold to move faster). Enter taps at the pointer. "
                + "Back, Home and volume retain their normal functions.\n\n"
                + "The pointer operates only while the original Totalplay app is in front. "
                + "It does not sign you in, read account data, or change the app's screen proportions. "
                + "Disable the accessibility service in system settings to turn it off.");
        directions.setTextSize(17);
        directions.setTextColor(0xffc7d4e9);
        directions.setPadding(0, 24, 0, 20);
        root.addView(directions);
        Button settings = new Button(this);
        settings.setText("1 • OPEN ACCESSIBILITY SETTINGS");
        settings.setOnClickListener(v -> {
            try { startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); }
            catch (Exception e) { Toast.makeText(this, "Open Android settings > Accessibility and enable Totalplay D-pad Pointer", Toast.LENGTH_LONG).show(); }
        });
        root.addView(settings);
        Button totalplay = new Button(this);
        totalplay.setText("2 • OPEN ORIGINAL TOTALPLAY");
        totalplay.setOnClickListener(v -> {
            Intent launch = getPackageManager().getLaunchIntentForPackage("com.TotalPlay.totalplay");
            if (launch == null) {
                Toast.makeText(this, "Install the original Totalplay app on this Android TV", Toast.LENGTH_LONG).show();
                return;
            }
            try { startActivity(launch); }
            catch (Exception e) { Toast.makeText(this, "Unable to open Totalplay", Toast.LENGTH_LONG).show(); }
        });
        root.addView(totalplay);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(root);
        setContentView(scroll);
        settings.requestFocus();
    }
}
