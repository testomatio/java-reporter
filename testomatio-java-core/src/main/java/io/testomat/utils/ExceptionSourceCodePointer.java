package io.testomat.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Lolik on 17.11.2023
 */
public class ExceptionSourceCodePointer {

  private static final String ANSI_RESET = "\u001b[0m";
  private static final String ANSI_RED = "\u001b[31m";
  private static final String ANSI_RED_BACKGROUND = "\u001b[41m";

  private static final String PATH = "src/test/java/";


  private static List<String> getLines(String filePath) {
    try {
      return Files.readAllLines(java.nio.file.Paths.get(PATH + filePath), java.nio.charset.StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static String getSourceCodeFragment(String filePath, int lineBegin, int lineEnd, int linePointer, boolean isColorsEnabled) {
    List<String> lines = getLines(filePath);
    StringBuilder sb = new StringBuilder();
    for (int i = lineBegin - 1; i < lineEnd; i++) {
      sb.append("\n");
      if (i == linePointer - 1) {
        sb.append(getMarkedLine(isColorsEnabled, lines.get(i), i));
      } else {
        sb.append("   ").append(i).append(" |").append(lines.get(i));
      }
    }
    return sb.toString();
  }

  private static String getMarkedLine(boolean isColorsEnabled, String line, int lineNumber){
      StringBuilder sb = new StringBuilder();
      if(isColorsEnabled) {
        sb.append(ANSI_RED_BACKGROUND).append(">>>").append(lineNumber).append(ANSI_RESET).append(" |")
            .append(ANSI_RED).append(line).append(ANSI_RESET);
      } else {
        sb.append(">> ").append(lineNumber).append(" |").append(line);
      }
      return sb.toString();
  }

  public static String parseExceptionSourceCodeFragment(String testMethodName, StackTraceElement[] stackTrace, boolean isColorsEnabled){
    try {
      StackTraceElement stackTraceElement =
          Arrays.stream(stackTrace).filter(e -> e.getMethodName().equals(testMethodName)).findFirst().orElseThrow();
      String filePath = stackTraceElement.getClassName().replace(".", "/") + ".java";
      int pointerLine = stackTraceElement.getLineNumber();
      return "Class: "+filePath + " Method: "+ testMethodName + getSourceCodeFragment(filePath, pointerLine - 5, pointerLine + 1, pointerLine, isColorsEnabled);
    } catch (Exception e) {
      return "ExceptionSourcePointer: Error occurred while parsing exception source code fragment";
    }
  }

}
