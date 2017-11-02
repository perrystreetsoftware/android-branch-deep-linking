package io.branch.util;

import android.os.Build;

import io.branch.referral.BuildConfig;

/**
 * Created by Perry Street Software Inc. on Nov 02, 2017.
 *
 * @author Steve Tsourounis {steve@scruff.com}
 */
public class EnvironmentConfig {
    private static final String FLAVOR_ENVIRONMENT_PROD = "prod";
    private static final String FLAVOR_ENVIRONMENT_DEV = "dev";

    public static boolean isProdEnvironment() {
        return BuildConfig.FLAVOR.equals(FLAVOR_ENVIRONMENT_PROD);
    }

    public static boolean isDevEnvironment() {
        return BuildConfig.FLAVOR.equals(FLAVOR_ENVIRONMENT_DEV);
    }

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
            return BuildConfig.GENYMOTION_LOCALHOST;
        }

        if (isRunningOnStockEmulator()) {
            return BuildConfig.EMULATOR_LOCALHOST;
        }

        return BuildConfig.DEVICE_LOCALHOST;
    }

    public static String getBaseUrl() {
        if (isProdEnvironment()) {
            return BuildConfig.PRODUCTION_BASE_URL;
        } else if (isDevEnvironment()) {
            return String.format("http://%s:8080", getDevServerHost());
        }

        throw new IllegalStateException("Unknown environment");
    }
}
