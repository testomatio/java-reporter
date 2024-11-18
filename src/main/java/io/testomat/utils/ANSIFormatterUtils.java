package io.testomat.utils;

/**
 * Created by Lolik on 11.07.2024
 */
public class ANSIFormatterUtils {

  private ANSIFormatterUtils() {
    throw new IllegalStateException("Utility class");
  }

  private static final String CODE = "```";

  // ANSI escape codes
  private static final String RESET = "\033[0m";
  private static final String BOLD = "\033[1m";
  private static final String ITALIC = "\033[3m";  // Italic text
  private static final String UNDERLINE = "\033[4m";
  private static final String REVERSED = "\033[7m";

  // Text colors
  private static final String BLACK_TEXT = "\033[30m";
  private static final String RED_TEXT = "\033[31m";
  private static final String GREEN_TEXT = "\033[32m";
  private static final String YELLOW_TEXT = "\033[33m";
  private static final String BLUE_TEXT = "\033[34m";
  private static final String MAGENTA_TEXT = "\033[35m";
  private static final String CYAN_TEXT = "\033[36m";
  private static final String WHITE_TEXT = "\033[37m";

  // Background colors
  private static final String BLACK_BACKGROUND = "\033[40m";
  private static final String RED_BACKGROUND = "\033[41m";
  private static final String GREEN_BACKGROUND = "\033[42m";
  private static final String YELLOW_BACKGROUND = "\033[43m";
  private static final String BLUE_BACKGROUND = "\033[44m";
  private static final String MAGENTA_BACKGROUND = "\033[45m";
  private static final String CYAN_BACKGROUND = "\033[46m";
  private static final String WHITE_BACKGROUND = "\033[47m";

  // Method to apply bold formatting
  public static String bold(String text) {
    return BOLD + text + RESET;
  }

  // Method to apply italic formatting
  public static String italic(String text) {
    return ITALIC + text + RESET;
  }

  // Method to apply underline formatting
  public static String underline(String text) {
    return UNDERLINE + text + RESET;
  }

  // Method to apply reversed formatting
  public static String reversed(String text) {
    return REVERSED + text + RESET;
  }

  // Method to apply text color
  public static String color(String text, String colorCode) {
    return colorCode + text + RESET;
  }

  // Method to apply background color
  public static String backgroundColor(String text, String colorCode) {
    return colorCode + text + RESET;
  }

  // Method to apply bold and text color
  public static String boldAndColor(String text, String colorCode) {
    return BOLD + colorCode + text + RESET;
  }

  // Method to apply bold, text color, and background color
  public static String boldColorAndBackground(String text, String textColor, String backgroundColor) {
    return BOLD + textColor + backgroundColor + text + RESET;
  }

  public static String code(String text) {
    if (text == null) {
      return null;
    } else {
      return CODE + text + CODE;
    }
  }
}
