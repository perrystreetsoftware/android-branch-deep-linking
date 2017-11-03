package io.branch.util;

import io.branch.referral.BuildConfig;

/**
 * Created by Perry Street Software Inc. on Nov 03, 2017.
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

  public static String getBaseUrl() {
    if (isProdEnvironment()) {
      return BuildConfig.PRODUCTION_BASE_URL;
    } else if (isDevEnvironment()) {
      return String.format("http://%s:8080", AndroidInfoHelpers.getDevServerHost());
    }

    throw new IllegalStateException("Unknown environment");
  }
}
