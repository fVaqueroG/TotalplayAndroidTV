package com.fv.totalplaytv.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Leanback launcher tile for the installed, signed Totalplay application.
 *
 * This activity is NOT the Totalplay player and does not modify its APK,
 * screen orientation, login, or playback behavior. Its only job is to make
 * the otherwise-hidden phone/tablet app accessible from the Android TV app row.
 */
public final class TotalplayLauncherActivity extends Activity {
    private static final String ORIGINAL_PACKAGE = "com.TotalPlay.totalplay";
    private static final String ORIGINAL_LAUNCHER =
            "mx.ixer62.qgNsk3.ffzCu7.kY3sL2.rWn3V6";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        Intent launch = getPackageManager().getLaunchIntentForPackage(ORIGINAL_PACKAGE);
        if (launch == null) {
            // Fallback for Totalplay 4.1.420 on the user's Android 9 TV:
            // this launcher entry was observed in the device's ActivityManager log.
            launch = new Intent(Intent.ACTION_MAIN);
            launch.addCategory(Intent.CATEGORY_LAUNCHER);
            launch.setComponent(new ComponentName(ORIGINAL_PACKAGE, ORIGINAL_LAUNCHER));
        }
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        try {
            startActivity(launch);
        } catch (RuntimeException error) {
            Toast.makeText(this,
                    "Could not open original Totalplay. Check that it is installed under System Apps.",
                    Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MainActivity.class));
        } finally {
            finish();
        }
    }
}
