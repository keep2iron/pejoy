package io.github.keep2iron.pejoy.utilities;

import android.os.Build;

/**
 * @author JoongWon Baik
 */
public class Platform {
    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }
}