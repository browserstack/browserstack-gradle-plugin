package com.browserstack.gradle;

public class Tools {

  public static boolean isStringEmpty(String str) {
    return str == null || str.length() == 0;
  }

  public static String capitalize(String variantName) throws Exception {
    if (isStringEmpty(variantName)) {
      throw new Exception("Null or empty variantName passed.");
    }
    return variantName.substring(0, 1).toUpperCase() + variantName.substring(1);
  }
}
