package com.browserstack.gradle;

/** Shared string and naming utilities for the BrowserStack Gradle plugin. */
public class Tools {

  /** Returns true if the string is null or has length zero. */
  public static boolean isStringEmpty(String str) {
    return str == null || str.length() == 0;
  }

  /**
   * Returns the string with the first character uppercased.
   * @param variantName non-null, non-empty string
   * @return capitalized string
   * @throws Exception if variantName is null or empty
   */
  public static String capitalize(String variantName) throws Exception {
    if (isStringEmpty(variantName)) {
      throw new Exception("Null or empty variantName passed.");
    }
    return variantName.substring(0, 1).toUpperCase() + variantName.substring(1);
  }
}
