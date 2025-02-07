package io.testomat.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by Lolik on 18.11.2024
 */
public class StringFormatterUtils {

  private StringFormatterUtils() {
    throw new IllegalStateException("Utility class");
  }

  public static String capitalizeAndSplit(String toCapitalize) {
    return StringUtils.capitalize(
        StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(toCapitalize),
        StringUtils.SPACE)
    );
  }

  public static String shorten(String input, int maxLength) {
    if (input.length() > maxLength) {
      return input.substring(0, maxLength - 3) + "...";
    } else {
      return input;
    }
  }

}
