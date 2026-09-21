package com.fv.totalplaytv.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** Testing only: authentication and playback remain in the official app. */
public final class MainActivity extends Activity {
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(32, 24, 32, 24);
        root.setBackgroundColor(0xff0b1425);
        TextView title = new TextView(this);
        title.setText("Totalplay TV • sign-in experiment");
        title.setTextColor(0xffffffff);
        title.setTextSize(28);
        root.addView(title);
        TextView status = new TextView(this);
        status.setText("Use the installed original Totalplay app to sign in securely. This experimental app cannot yet access your session, channel list, or protected video playback. No decoder connection is used.");
        status.setTextColor(0xffb8c9e2);
        status.setTextSize(17);
        status.setPadding(0, 24, 0, 24);
        root.addView(status);
        Button signIn = new Button(this);
        signIn.setText("OPEN OFFICIAL TOTALPLAY / SIGN IN");
        signIn.setOnClickListener(v -> {
            Intent launch = getPackageManager().getLaunchIntentForPackage("com.TotalPlay.totalplay");
            if (launch == null) {
                Toast.makeText(this, "Install the original Totalplay app on this device", Toast.LENGTH_LONG).show();
                return;
            }
            try { startActivity(launch); }
            catch (Exception e) { Toast.makeText(this, "Could not open Totalplay", Toast.LENGTH_LONG).show(); }
        });
        root.addView(signIn);
        setContentView(root);
    }
}
