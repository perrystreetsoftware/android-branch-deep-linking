package io.branch.util;

import android.os.Build;

import io.branch.referral.BuildConfig;

/**
 * Created by Perry Street Software Inc. on Nov 02, 2017.
 * https://github.com/facebook/react-native/blob/master/ReactAndroid/src/main/java/com/facebook/react/modules/systeminfo/AndroidInfoHelpers.java
 *
 * @author Steve Tsourounis {steve@scruff.com}
 */
public class AndroidInfoHelpers {

    public static final String EMULATOR_LOCALHOST = "10.0.2.2";
    public static final String GENYMOTION_LOCALHOST = "10.0.3.2";
    public static final String DEVICE_LOCALHOST = "localhost";

    public static boolean isRunningOnGenymotion() {
        return Build.FINGERPRINT.contains("vbox");
    }

    public static boolean isRunningOnStockEmulator() {
        return Build.FINGERPRINT.contains("generic");
    }

    public static String getDevServerHost() {
        // Since genymotion runs in vbox it use different hostname to refer to adb host.
        // We detect whether app runs on genymotion and replace js bundle server hostname accordingly

        if (isRunningOnGenymotion()) {
            return GENYMOTION_LOCALHOST;
        }

        if (isRunningOnStockEmulator()) {
            return EMULATOR_LOCALHOST;
        }

        return BuildConfig.DEVICE_LOCALHOST != null ?
                BuildConfig.DEVICE_LOCALHOST : DEVICE_LOCALHOST;
    }

    public static String getFriendlyDeviceName() {
        if (isRunningOnGenymotion()) {
            // Genymotion already has a friendly name by default
            return Build.MODEL;
        } else {
            return Build.MODEL + " - " + Build.VERSION.RELEASE + " - API " + Build.VERSION.SDK_INT;
        }
    }
}
